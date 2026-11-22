// Front du prototype. Le back vérifie toutes les règles ; l'écran affiche ses messages tels quels.
// Seule exception : l'aperçu de la pièce à l'étape 2, calculé ici pour aider la saisie.
// Le moteur reste seul juge à l'envoi.
const API = window.MOTEUR_API.replace(/\/$/, "");
const $ = (s, el = document) => el.querySelector(s);
const $$ = (s, el = document) => [...el.querySelectorAll(s)];
let param = null;
let pieceAffichee = null;

// ---------- Rôle courant (traçabilité et règle des 4 yeux) ----------
const choixRole = $("#role");
function lireStockage(cle) { try { return localStorage.getItem(cle); } catch { return null; } }
function ecrireStockage(cle, v) { try { localStorage.setItem(cle, v); } catch { /* navigation privée */ } }
choixRole.value = lireStockage("role") || "gestionnaire";
const utilisateur = () => choixRole.value;
choixRole.onchange = () => {
  ecrireStockage("role", choixRole.value);
  message(`Vous agissez maintenant en tant que ${choixRole.value}.`);
  if (pieceAffichee) afficherPiece(pieceAffichee, $("#detail-ecriture"));
};

// ---------- API ----------
async function api(chemin, options = {}) {
  let rep;
  try {
    rep = await fetch(API + chemin, { ...options, headers: { "Content-Type": "application/json", "X-Utilisateur": utilisateur() } });
  } catch {
    throw new Error("Le serveur ne répond pas. S'il était en veille, réessayez dans une minute.");
  }
  if (rep.status === 204) return null;
  const corps = await rep.json().catch(() => null);
  if (!rep.ok) {
    const erreur = new Error(corps?.message || `Le serveur a répondu ${rep.status}.`);
    erreur.code = corps?.code;
    throw erreur;
  }
  return corps;
}
const post = (chemin, corps) => api(chemin, { method: "POST", body: JSON.stringify(corps ?? {}) });

// ---------- Messages ----------
let minuterie;
function message(texte, ok = true) {
  const m = $("#message");
  m.textContent = texte;
  m.className = "message " + (ok ? "ok" : "ko");
  m.hidden = false;
  clearTimeout(minuterie);
  minuterie = setTimeout(() => { m.hidden = true; }, ok ? 4000 : 10000);
}

// ---------- Construction d'éléments (textContent : aucune injection HTML) ----------
function el(balise, attrs = {}, ...enfants) {
  const n = document.createElement(balise);
  Object.entries(attrs).forEach(([k, v]) => {
    if (v === undefined || v === false) return;
    if (k === "class") n.className = v;
    else if (k.startsWith("on")) n[k] = v;
    else n.setAttribute(k, v === true ? "" : v);
  });
  enfants.flat().forEach((e) => n.append(e instanceof Node ? e : document.createTextNode(e ?? "")));
  return n;
}
const montant = (n) => (n == null || n === "" ? "" : Number(n).toLocaleString("fr-FR", { maximumFractionDigits: 2 }));
const LIBELLES_STATUT = { PROPOSEE: "À valider", VALIDEE: "Validée", ANNULEE: "Annulée", BROUILLON: "Brouillon", PUBLIEE: "Publiée", RETIREE: "Retirée" };
const statut = (s) => el("span", { class: "statut " + s }, LIBELLES_STATUT[s] || s);
const rubrique = (code) => param?.rubriques.find((r) => r.code === code)?.libelle || code;
const evenement = (code) => param?.evenements.find((e) => e.code === code)?.libelle || code;
const dateFr = (d) => (d ? new Date(d).toLocaleDateString("fr-FR") : "");

// ---------- Navigation ----------
function allerA(etape) {
  $$(".etape").forEach((b) => b.classList.toggle("actif", b.dataset.etape === etape));
  $$(".panneau").forEach((p) => { p.hidden = p.id !== etape; });
  if (etape === "generer") preparerGeneration();
  if (etape === "ecritures") chargerEcritures();
  if (etape === "retours") chargerRetours();
  window.scrollTo(0, 0);
}
$$(".etape").forEach((b) => b.onclick = () => allerA(b.dataset.etape));
$$("#sous-plan button").forEach((b) => b.onclick = () => {
  allerA("parametrage");
  $$("#sous-plan button").forEach((x) => x.classList.toggle("actif", x === b));
  $$(".bloc").forEach((p) => { p.hidden = p.dataset.bloc !== b.dataset.param; });
});

// =================== 1. PARAMÉTRER ===================
async function chargerParametrage() {
  param = await api("/api/parametrage");
  for (const k of ["operations", "journaux", "evenements", "rubriques", "comptes", "mappings", "recettes"]) {
    $("#n-" + k).textContent = param[k].length;
    $(`#sous-plan button[data-param="${k}"]`).classList.toggle("rempli", param[k].length > 0);
  }
  remplirListes();
  afficherListes();
  afficherRecettes();
}

function remplirListes() {
  const sources = {
    operations: param.operations.map((o) => [o.code, `${o.code} · ${o.libelle ?? ""}`]),
    journaux: param.journaux.map((j) => [j.code, `${j.code} · ${j.libelle ?? ""}`]),
    evenements: param.evenements.map((e) => [e.code, e.libelle ?? e.code]),
    rubriques: param.rubriques.map((r) => [r.code, `${r.code} · ${r.libelle ?? ""}`]),
    comptes: param.comptes.map((c) => [c.code, `${c.code} · ${c.libelle ?? ""}`]),
  };
  $$("select[data-liste]").forEach((s) => {
    const choisi = s.value;
    s.replaceChildren(...sources[s.dataset.liste].map(([v, t]) => new Option(t, v)));
    if (choisi && sources[s.dataset.liste].some(([v]) => v === choisi)) s.value = choisi;
  });
}

// Suppression : directe si l'élément est libre ; sinon le moteur dit ce qui en dépend,
// et l'on propose de supprimer aussi ces éléments (cascade).
function effacer(chemin, nom) {
  return el("button", { type: "button", class: "effacer", "aria-label": `Supprimer ${nom}`, onclick: async () => {
    if (!confirm(`Supprimer ${nom} ?`)) return;
    const fait = () => { message(`${nom[0].toUpperCase() + nom.slice(1)} : supprimé.`); return chargerParametrage(); };
    try { await api(chemin, { method: "DELETE" }); await fait(); }
    catch (e) {
      if (e.code !== "ELEMENT_UTILISE") { message(e.message, false); return; }
      if (!confirm(`${e.message}\n\nSupprimer aussi ce qui en dépend ? Les écritures déjà créées ne sont pas touchées.`)) return;
      try { await api(chemin + "?cascade=true", { method: "DELETE" }); await fait(); }
      catch (e2) { message(e2.message, false); }
    }
  } }, "Supprimer");
}

function tableau(id, lignes, nbColonnes, siVide) {
  const corps = $("#" + id);
  if (!lignes.length) { corps.replaceChildren(el("tr", {}, el("td", { colspan: nbColonnes, class: "vide" }, siVide))); return; }
  corps.replaceChildren(...lignes.map((cellules) => el("tr", {}, cellules.map((c) => el("td", {}, c)))));
}

function afficherListes() {
  const natures = { ACTIF: "Actif", CHARGE: "Charge", PASSIF: "Passif", PRODUIT: "Produit" };
  const tous = (v) => (v === "*" ? "toutes" : v);
  tableau("t-operations", param.operations.map((o) => [o.code, o.libelle, o.createdBy,
    effacer(`/api/parametrage/operations/${encodeURIComponent(o.code)}`, `l'opération ${o.code}`)]), 4,
    "Aucune opération. Ajoutez la première avec la ligne ci-dessus.");
  tableau("t-journaux", param.journaux.map((j) => [j.code, j.libelle, j.codeExterne || "—",
    effacer(`/api/parametrage/journaux/${encodeURIComponent(j.code)}`, `le journal ${j.code}`)]), 4,
    "Aucun journal pour l'instant.");
  tableau("t-evenements", param.evenements.map((e) => [e.code, e.libelle, e.operationCode, e.journalCode,
    effacer(`/api/parametrage/evenements/${encodeURIComponent(e.code)}`, `l'événement ${e.code}`)]), 5,
    "Aucun événement. Il faut d'abord une opération et un journal.");
  tableau("t-rubriques", param.rubriques.map((r) => [r.code, r.libelle, natures[r.nature],
    effacer(`/api/parametrage/rubriques/${encodeURIComponent(r.code)}`, `la rubrique ${r.code}`)]), 4,
    "Aucune rubrique pour l'instant.");
  tableau("t-comptes", param.comptes.map((c) => [c.code, c.libelle, c.createdBy,
    effacer(`/api/parametrage/comptes/${encodeURIComponent(c.code)}`, `le compte ${c.code}`)]), 4,
    "Aucun compte pour l'instant.");
  tableau("t-mappings", param.mappings.map((m) => [`${m.rubriqueCode} · ${rubrique(m.rubriqueCode)}`,
    tous(m.classeActif), tous(m.categorie), m.compteCode,
    effacer(`/api/parametrage/mappings/${m.id}`, `la ligne ${m.rubriqueCode} → ${m.compteCode}`)]), 5,
    "Aucune ligne. Sans mapping, le moteur ne sait pas dans quel compte écrire.");
}

function afficherRecettes() {
  const zone = $("#t-recettes");
  if (!param.recettes.length) { zone.replaceChildren(el("p", { class: "vide" }, "Aucune recette. Sans recette, un événement ne produit pas d'écriture.")); return; }
  zone.replaceChildren(...[...param.recettes].reverse().map((r) => {
    const actions = [];
    if (r.statut === "BROUILLON") actions.push(el("button", { type: "button", class: "lien", onclick: async () => {
      try { await post(`/api/parametrage/recettes/${r.id}/publier`, { utilisateur: utilisateur() }); message("Recette publiée."); await chargerParametrage(); }
      catch (e) { message(e.message, false); } } }, "Publier"),
      el("button", { type: "button", class: "effacer", onclick: async () => {
        if (!confirm("Supprimer ce brouillon de recette ?")) return;
        try { await api(`/api/parametrage/recettes/${r.id}`, { method: "DELETE" }); message("Brouillon supprimé."); await chargerParametrage(); }
        catch (e) { message(e.message, false); } } }, "Supprimer"));
    if (r.statut === "PUBLIEE") actions.push(el("button", { type: "button", class: "effacer", onclick: async () => {
      if (!confirm("Retirer cette recette ? Elle ne servira plus aux nouveaux événements. Les écritures déjà créées la gardent.")) return;
      try { await post(`/api/parametrage/recettes/${r.id}/retirer`); message("Recette retirée."); await chargerParametrage(); }
      catch (e) { message(e.message, false); } } }, "Retirer"));
    return el("div", { class: "recette" },
      el("h2", {}, `${evenement(r.evenementCode)}, version ${r.version}`, statut(r.statut), ...actions),
      el("p", { class: "meta" }, `Valable à partir du ${dateFr(r.dateEffet)}` + (r.publiePar ? `. Publiée par ${r.publiePar}.` : ".")),
      el("table", {}, el("tbody", {}, r.lignes.map((l) => el("tr", {},
        el("td", {}, `${l.rubriqueCode} · ${rubrique(l.rubriqueCode)}`),
        el("td", { class: "sens-" + l.sens }, l.sens === "D" ? "Débit" : "Crédit"),
        el("td", {}, l.montantSource),
        el("td", {}, l.ignorerSiZero ? "facultative" : ""))))));
  }));
}

function brancher(idForm, chemin, nom) {
  $("#" + idForm).onsubmit = async (ev) => {
    ev.preventDefault();
    try {
      await post(chemin, Object.fromEntries(new FormData(ev.target).entries()));
      message(`${nom} ajouté.`);
      ev.target.reset();
      await chargerParametrage();
    } catch (e) { message(e.message, false); }
  };
}
brancher("f-operations", "/api/parametrage/operations", "Opération");
brancher("f-journaux", "/api/parametrage/journaux", "Journal");
brancher("f-evenements", "/api/parametrage/evenements", "Événement");
brancher("f-rubriques", "/api/parametrage/rubriques", "Rubrique");
brancher("f-comptes", "/api/parametrage/comptes", "Compte");
brancher("f-mappings", "/api/parametrage/mappings", "Mapping");

// Recette : lignes dynamiques et contrôle en direct
function ajouterLigneRecette(sens = "D") {
  const rub = el("select", { "aria-label": "Rubrique" }, param.rubriques.map((r) => new Option(`${r.code} · ${r.libelle ?? ""}`, r.code)));
  const cote = el("select", { "aria-label": "Côté" }, new Option("Débit, où va la valeur", "D"), new Option("Crédit, d'où elle part", "C"));
  cote.value = sens;
  const source = el("input", { "aria-label": "Montant reçu à utiliser", placeholder: "nominal", required: true });
  const fac = el("input", { type: "checkbox", "aria-label": "Ligne facultative" });
  const tr = el("tr", {}, el("td", {}, rub), el("td", {}, cote), el("td", {}, source), el("td", {}, fac),
    el("td", {}, el("button", { type: "button", class: "effacer", "aria-label": "Retirer la ligne", onclick: () => { tr.remove(); controleRecette(); } }, "Retirer")));
  [rub, cote, source, fac].forEach((x) => x.addEventListener("input", controleRecette));
  $("#lignes-recette").append(tr);
  controleRecette();
}
const lignesSaisies = () => $$("#lignes-recette tr").map((tr) => {
  const [rub, cote, source, fac] = $$("select, input", tr);
  return { rubrique: rub.value, sens: cote.value, montantSource: source.value.trim(), ignorerSiZero: fac.checked };
});
function controleRecette() {
  const l = lignesSaisies();
  const ok = l.some((x) => x.sens === "D") && l.some((x) => x.sens === "C");
  const p = $("#apercu-recette");
  p.className = "controle " + (ok ? "ok" : "ko");
  p.textContent = ok ? "La recette a au moins une ligne au débit et une au crédit." : "Il manque une ligne au débit ou au crédit.";
}
$("#ajouter-ligne").onclick = () => ajouterLigneRecette();
$("#f-recettes").onsubmit = async (ev) => {
  ev.preventDefault();
  const f = ev.target;
  try {
    const r = await post("/api/parametrage/recettes", { evenementCode: f.evenementCode.value, dateEffet: f.dateEffet.value || null,
      utilisateur: utilisateur(), publier: f.publier.checked, lignes: lignesSaisies() });
    message(`Recette enregistrée en version ${r.version}.`);
    $("#lignes-recette").replaceChildren();
    ajouterLigneRecette("D"); ajouterLigneRecette("C");
    await chargerParametrage();
  } catch (e) { message(e.message, false); }
};

// =================== 2. GÉNÉRER ===================
const EXEMPLES = {
  ACQ_OBLIGATION: { nominal: 2000000, frais: 20000, total_paye: 2020000 },
  COUPON_OBLIGATION: { coupon: 120000 },
  REMB_OBLIGATION: { nominal: 2000000 },
};
const recetteEnVigueur = (code) => param.recettes.filter((r) => r.evenementCode === code && r.statut === "PUBLIEE").sort((a, b) => b.version - a.version)[0];

async function preparerGeneration() {
  if (!param) await chargerParametrage();
  const sel = $("#g-evenement");
  const choisi = sel.value;
  const prets = param.evenements.filter((e) => recetteEnVigueur(e.code));
  sel.replaceChildren(...prets.map((e) => new Option(e.libelle ?? e.code, e.code)));
  if (choisi && prets.some((e) => e.code === choisi)) sel.value = choisi;
  if (!$("#g-date").value) $("#g-date").value = new Date().toISOString().slice(0, 10);
  nouvelIdentifiant();
  champsMontants();
}
function nouvelIdentifiant() { $("#g-eventId").value = `${$("#g-evenement").value || "EVT"}-${Date.now().toString(36).toUpperCase()}`; }
function champsMontants() {
  const zone = $("#g-montants");
  const r = recetteEnVigueur($("#g-evenement").value);
  if (!r) { zone.replaceChildren(el("p", { class: "vide" }, "Aucun événement n'a de recette publiée. Créez-en une à l'étape 1.")); apercu(); return; }
  const noms = new Map();
  r.lignes.forEach((l) => noms.set(l.montantSource, (noms.get(l.montantSource) ?? true) && l.ignorerSiZero));
  zone.replaceChildren(...[...noms].map(([nom, facultatif]) => el("label", {},
    el("span", {}, nom.replaceAll("_", " "), facultatif ? el("small", {}, "facultatif") : ""),
    el("input", { type: "number", step: "0.01", min: "0", inputmode: "decimal", name: nom, required: !facultatif, oninput: apercu }))));
  apercu();
}
["#g-evenement", "#g-classe", "#g-categorie", "#g-date"].forEach((s) => $(s).addEventListener("input", () => {
  if (s === "#g-evenement") { nouvelIdentifiant(); champsMontants(); } else apercu();
}));
$("#g-exemple").onclick = () => {
  const ex = EXEMPLES[$("#g-evenement").value] || {};
  $$("#g-montants input").forEach((i) => { i.value = ex[i.name] ?? (i.required ? 1000 : ""); });
  apercu();
};

// Aperçu de la pièce, recalculé à chaque saisie (même règle que le moteur : la ligne de mapping la plus précise)
function compteAttendu(rub, classe, categorie) {
  const score = (m) => {
    let s = 0;
    if (m.classeActif === classe) s += 2; else if (m.classeActif !== "*") return -1;
    if (m.categorie === categorie) s += 1; else if (m.categorie !== "*") return -1;
    return s;
  };
  return param.mappings.filter((m) => m.rubriqueCode === rub && score(m) >= 0).sort((a, b) => score(b) - score(a))[0]?.compteCode;
}
function apercu() {
  const zone = $("#g-resultat");
  const code = $("#g-evenement").value;
  const r = code && recetteEnVigueur(code);
  if (!r) { zone.replaceChildren(); return; }
  const classe = ($("#g-classe").value || "*").trim().toUpperCase();
  const categorie = ($("#g-categorie").value || "*").trim().toUpperCase();
  const saisis = Object.fromEntries($$("#g-montants input").map((i) => [i.name, i.value === "" ? null : Number(i.value)]));
  const lignes = r.lignes
    .filter((l) => !(l.ignorerSiZero && !saisis[l.montantSource]))
    .map((l) => ({ rubrique: l.rubriqueCode, compte: compteAttendu(l.rubriqueCode, classe, categorie),
      debit: l.sens === "D" ? saisis[l.montantSource] : null, credit: l.sens === "C" ? saisis[l.montantSource] : null }));
  const totalD = lignes.reduce((s, l) => s + (l.debit || 0), 0);
  const totalC = lignes.reduce((s, l) => s + (l.credit || 0), 0);
  const rempli = Object.values(saisis).some((v) => v);
  zone.replaceChildren(piece({
    apercu: true, numero: null, titre: "Aperçu de la pièce", info: `${evenement(code)} · recette version ${r.version}`,
    lignes, totalD, totalC,
    balance: !rempli ? ["neutre", "Saisissez les montants pour voir l'écriture se former.", ""]
      : Math.abs(totalD - totalC) < 0.005 ? ["ok", "Équilibrée : débit = crédit", `${montant(totalD)} = ${montant(totalC)}`]
        : ["ko", "Déséquilibrée : le moteur refusera", `${montant(totalD)} ≠ ${montant(totalC)}`],
  }));
}

$("#f-generer").onsubmit = async (ev) => {
  ev.preventDefault();
  const f = ev.target;
  const montants = {};
  $$("#g-montants input").forEach((i) => { if (i.value !== "") montants[i.name] = Number(i.value); });
  try {
    const r = await post("/api/evenements", { source: "PAGE_TEST", eventId: f.eventId.value, evenement: f.evenement.value,
      dateValeur: f.dateValeur.value, classeActif: f.classeActif.value, categorie: f.categorie.value,
      referenceTitre: f.referenceTitre.value || null, utilisateur: utilisateur(), montants });
    message(r.statut === "DOUBLON" ? "Cet identifiant a déjà été reçu : voici l'écriture existante. Aucune nouvelle n'a été créée."
      : "Écriture créée. Elle attend sa validation à l'étape 3.");
    afficherPiece(r.ecriture, $("#g-resultat"));
    if (r.statut !== "DOUBLON") nouvelIdentifiant();
  } catch (e) {
    apercu();
    $("#g-resultat").prepend(el("div", { class: "refus" }, el("b", {}, "Le moteur a refusé l'événement"), e.message));
  }
};

// =================== La pièce comptable ===================
function piece({ apercu: estApercu, numero, titre, info, info2, statutPiece, lignes, totalD, totalC, balance, actions }) {
  return el("article", { class: "piece" + (estApercu ? " apercu" : "") },
    el("div", { class: "piece-tete" },
      el("div", {},
        el("div", { class: "piece-numero" + (numero ? "" : " brouillon") }, numero || titre),
        el("div", { class: "piece-info" }, info),
        info2 ? el("div", { class: "piece-info" }, info2) : ""),
      statutPiece ? statut(statutPiece) : ""),
    el("table", {},
      el("thead", {}, el("tr", {}, el("th", {}, "Ligne"), el("th", { class: "col-debit" }, "Débit"), el("th", { class: "col-credit" }, "Crédit"))),
      el("tbody", {}, lignes.map((l) => el("tr", {},
        el("td", {}, `${l.rubrique} · ${rubrique(l.rubrique)}`,
          el("span", { class: "compte" + (l.compte ? "" : " manque") }, l.compte ? `compte ${l.compte}` : "aucun compte dans le mapping")),
        el("td", { class: "col-debit" }, montant(l.debit)),
        el("td", { class: "col-credit" }, montant(l.credit))))),
      el("tfoot", {}, el("tr", {}, el("td", {}, "Total"), el("td", { class: "col-debit" }, montant(totalD)), el("td", { class: "col-credit" }, montant(totalC))))),
    el("div", { class: "balance " + balance[0] }, el("span", {}, balance[1]), el("span", {}, balance[2])),
    actions || "");
}

function afficherPiece(e, conteneur) {
  if (conteneur.id === "detail-ecriture") pieceAffichee = e;
  const actions = el("div", { class: "piece-actions" });
  if (e.statut === "PROPOSEE") {
    actions.append(el("p", {}, e.creePar === utilisateur()
      ? "Vous avez créé cette écriture. Passez en « Comptable » en haut de la page pour la valider."
      : "Valider donne un numéro à l'écriture et la fige."));
    actions.append(el("button", { type: "button", class: "secondaire", onclick: async () => {
      if (!confirm("Annuler cette écriture ? Elle restera visible avec le statut « Annulée ».")) return;
      try {
        const a = await post(`/api/ecritures/${e.id}/annuler`, { utilisateur: utilisateur() });
        message("Écriture annulée.");
        afficherPiece(a, conteneur);
        if (!$("#ecritures").hidden) chargerEcritures(a.id);
      } catch (err) { message(err.message, false); }
    } }, "Annuler l'écriture"));
    actions.append(el("button", { type: "button", onclick: async () => {
      try {
        const v = await post(`/api/ecritures/${e.id}/valider`, { utilisateur: utilisateur() });
        message(`Écriture validée sous le numéro ${v.numero}.`);
        afficherPiece(v, conteneur);
        if (!$("#ecritures").hidden) chargerEcritures(v.id);
      } catch (err) { message(err.message, false); }
    } }, "Valider l'écriture"));
  } else if (e.statut === "ANNULEE") {
    actions.append(el("p", {}, `Annulée${e.annuleePar ? " par " + e.annuleePar : ""}. Elle reste visible pour la traçabilité et ne compte plus.`));
  } else if (e.statut === "VALIDEE") {
    actions.append(el("p", {}, "Validée : elle ne changera plus. Une erreur se corrige par une écriture inverse."));
  }
  conteneur.replaceChildren(piece({
    numero: e.numero, titre: "Pas encore numérotée",
    info: `${evenement(e.evenement)} · journal ${e.journal} · ${dateFr(e.dateValeur)}`,
    info2: `Recette version ${e.recetteVersion} · créée par ${e.creePar}` + (e.valideePar ? ` · validée par ${e.valideePar}` : ""),
    statutPiece: e.statut, totalD: e.totalDebit, totalC: e.totalCredit,
    lignes: e.lignes.map((l) => ({ rubrique: l.rubrique, compte: l.compte, debit: l.debit, credit: l.credit })),
    balance: e.equilibree ? ["ok", "Équilibrée : débit = crédit", `${montant(e.totalDebit)} = ${montant(e.totalCredit)} XOF`] : ["ko", "Déséquilibrée", ""],
    actions,
  }));
}

// =================== 3. VALIDER ===================
async function chargerEcritures(idChoisi) {
  if (!param) await chargerParametrage();
  let liste;
  try { liste = await api("/api/ecritures"); } catch (e) { message(e.message, false); return; }
  const corps = $("#t-ecritures");
  if (!liste.length) { corps.replaceChildren(el("tr", {}, el("td", { colspan: 4, class: "vide" }, "Aucune écriture. Générez-en une à l'étape 2."))); return; }
  corps.replaceChildren(...liste.map((e) => {
    const tr = el("tr", { tabindex: 0 }, el("td", {}, e.numero || "à numéroter"), el("td", {}, evenement(e.evenement)),
      el("td", {}, statut(e.statut)), el("td", { class: "num" }, montant(e.totalDebit)));
    const ouvrir = () => { $$("#t-ecritures tr").forEach((x) => x.classList.remove("choisi")); tr.classList.add("choisi"); afficherPiece(e, $("#detail-ecriture")); };
    tr.onclick = ouvrir;
    tr.onkeydown = (k) => { if (k.key === "Enter" || k.key === " ") { k.preventDefault(); ouvrir(); } };
    if (idChoisi === e.id) queueMicrotask(ouvrir);
    return tr;
  }));
}

// =================== 4. RETOURS ===================
const STATUTS_RETOUR = { NON_TESTE: "Non testé", VALIDE: "Valide", PAS_VALIDE: "Pas valide", RESTE_A_FAIRE: "Reste à faire" };
let retours = [];

async function chargerRetours() {
  try { retours = await api("/api/retours"); } catch (e) { message(e.message, false); return; }
  afficherRetours();
}

function afficherBilan() {
  const compte = (s) => retours.filter((r) => r.statut === s).length;
  $("#r-bilan").replaceChildren(...Object.entries(STATUTS_RETOUR).map(([code, libelle]) =>
    el("div", { class: code }, el("strong", {}, String(compte(code))), el("span", {}, libelle))));
}

function afficherRetours() {
  afficherBilan();
  const filtre = $("#r-filtre").value;
  const liste = retours.filter((r) => !filtre || r.statut === filtre);
  const corps = $("#t-retours");
  if (!liste.length) { corps.replaceChildren(el("tr", {}, el("td", { colspan: 5, class: "vide" }, "Aucun point pour ce filtre."))); return; }
  corps.replaceChildren(...liste.map((r) => {
    const etat = el("span", { class: "enregistre" });
    const choix = el("select", { "aria-label": `Statut du point ${r.ordre}` },
      Object.entries(STATUTS_RETOUR).map(([v, t]) => new Option(t, v)));
    choix.value = r.statut;
    const obs = el("textarea", { "aria-label": `Observation sur le point ${r.ordre}`, placeholder: "Ce qui ne va pas, ce qui manque…" });
    obs.value = r.observation ?? "";
    const tr = el("tr", { class: r.statut },
      el("td", {}, String(r.ordre)),
      el("td", {}, el("span", { class: "fonction" }, r.fonctionnalite), r.critere, r.ouTester ? el("span", { class: "ou" }, `Où : ${r.ouTester}`) : ""),
      el("td", {}, choix, etat),
      el("td", {}, obs),
      el("td", {}, r.ajouteParTesteur ? el("button", { type: "button", class: "effacer", onclick: async () => {
        if (!confirm("Supprimer ce point ?")) return;
        try { await api(`/api/retours/${r.id}`, { method: "DELETE" }); await chargerRetours(); } catch (e) { message(e.message, false); }
      } }, "Supprimer") : ""));
    const enregistrer = async () => {
      if (choix.value === r.statut && (obs.value.trim() || "") === (r.observation ?? "")) return;
      etat.textContent = "Enregistrement…";
      try {
        const maj = await api(`/api/retours/${r.id}`, { method: "PUT", body: JSON.stringify({ statut: choix.value, observation: obs.value }) });
        Object.assign(r, maj);
        tr.className = r.statut;
        etat.textContent = `Enregistré par ${maj.updatedBy ?? utilisateur()}`;
        afficherBilan();
      } catch (e) { etat.textContent = ""; message(e.message, false); }
    };
    choix.onchange = enregistrer;
    obs.onblur = enregistrer;
    return tr;
  }));
}
$("#r-filtre").onchange = afficherRetours;

$("#f-retour").onsubmit = async (ev) => {
  ev.preventDefault();
  try {
    await post("/api/retours", Object.fromEntries(new FormData(ev.target).entries()));
    message("Point ajouté à la grille.");
    ev.target.reset();
    await chargerRetours();
  } catch (e) { message(e.message, false); }
};

// Export CSV (séparateur point-virgule, BOM UTF-8 : s'ouvre directement dans Excel)
$("#r-csv").onclick = () => {
  const champ = (v) => `"${String(v ?? "").replaceAll('"', '""')}"`;
  const lignes = [["N°", "Fonctionnalité", "Où tester", "Ce qu'il faut vérifier", "Statut", "Observation", "Modifié par", "Modifié le"],
    ...retours.map((r) => [r.ordre, r.fonctionnalite, r.ouTester, r.critere, STATUTS_RETOUR[r.statut], r.observation, r.updatedBy, r.updatedAt])];
  const csv = "\uFEFF" + lignes.map((l) => l.map(champ).join(";")).join("\r\n");
  const lien = el("a", { href: URL.createObjectURL(new Blob([csv], { type: "text/csv;charset=utf-8" })), download: "retours-moteur-comptable.csv" });
  document.body.append(lien); lien.click(); lien.remove();
};

// ---------- Démarrage ----------
chargerParametrage()
  .then(() => { ajouterLigneRecette("D"); ajouterLigneRecette("C"); })
  .catch((e) => message(e.message, false));
