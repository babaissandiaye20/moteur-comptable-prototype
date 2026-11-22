// Front du prototype. Aucune règle métier ici : le back vérifie tout,
// et l'écran affiche ses messages tels quels.
const API = window.MOTEUR_API.replace(/\/$/, "");
const $ = (s, el = document) => el.querySelector(s);
const $$ = (s, el = document) => [...el.querySelectorAll(s)];
let param = null;
let pieceAffichee = null; // écriture ouverte dans l'étape 3

// ---------- Rôle courant (traçabilité et règle des 4 yeux) ----------
const champAutre = $("#utilisateur");
let utilisateurCourant = lireStockage("utilisateur") || "gestionnaire";
function lireStockage(cle) { try { return localStorage.getItem(cle); } catch { return null; } }
function ecrireStockage(cle, v) { try { localStorage.setItem(cle, v); } catch { /* navigation privée */ } }
function choisirUtilisateur(nom) {
  utilisateurCourant = nom.trim() || "gestionnaire";
  ecrireStockage("utilisateur", utilisateurCourant);
  $$(".role-choix").forEach((b) => b.setAttribute("aria-pressed", String(b.dataset.role === utilisateurCourant)));
  champAutre.value = ["gestionnaire", "comptable"].includes(utilisateurCourant) ? "" : utilisateurCourant;
  if (pieceAffichee) afficherPiece(pieceAffichee, $("#detail-ecriture"));
}
$$(".role-choix").forEach((b) => b.onclick = () => choisirUtilisateur(b.dataset.role));
champAutre.addEventListener("change", () => choisirUtilisateur(champAutre.value));
choisirUtilisateur(utilisateurCourant);

// ---------- Appels à l'API ----------
async function api(chemin, options = {}) {
  let rep;
  try {
    rep = await fetch(API + chemin, {
      ...options,
      headers: { "Content-Type": "application/json", "X-Utilisateur": utilisateurCourant },
    });
  } catch {
    throw new Error("Le serveur ne répond pas. S'il était en veille, réessayez dans une minute.");
  }
  if (rep.status === 204) return null;
  const corps = await rep.json().catch(() => null);
  if (!rep.ok) throw new Error(corps?.message || `Le serveur a répondu ${rep.status}.`);
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

// ---------- Petites aides d'affichage (textContent : aucune injection HTML) ----------
function el(balise, attrs = {}, ...enfants) {
  const n = document.createElement(balise);
  Object.entries(attrs).forEach(([k, v]) => {
    if (k === "class") n.className = v;
    else if (k.startsWith("on")) n[k] = v;
    else n.setAttribute(k, v);
  });
  enfants.flat().forEach((e) => n.append(e instanceof Node ? e : document.createTextNode(e ?? "")));
  return n;
}
const montant = (n) => n == null ? "" : Number(n).toLocaleString("fr-FR", { maximumFractionDigits: 2 });
const statut = (s) => el("span", { class: "statut " + s }, {
  PROPOSEE: "À valider", VALIDEE: "Validée", ANNULEE: "Annulée",
  BROUILLON: "Brouillon", PUBLIEE: "Publiée", RETIREE: "Retirée",
}[s] || s);
const libelleRubrique = (code) => param?.rubriques.find((r) => r.code === code)?.libelle || code;
const libelleEvenement = (code) => param?.evenements.find((e) => e.code === code)?.libelle || code;

// ---------- Navigation ----------
function allerA(etape) {
  $$(".etape").forEach((b) => b.classList.toggle("actif", b.dataset.etape === etape));
  $$(".panneau").forEach((p) => { p.hidden = p.id !== etape; });
  if (etape === "ecritures") chargerEcritures();
  if (etape === "generer") preparerGeneration();
}
$$(".etape").forEach((b) => b.onclick = () => allerA(b.dataset.etape));
$$(".chaine button").forEach((b) => b.onclick = () => {
  $$(".chaine button").forEach((x) => x.classList.toggle("actif", x === b));
  $$(".bloc").forEach((p) => { p.hidden = p.dataset.bloc !== b.dataset.param; });
});

// =================== 1. PARAMÉTRER ===================
async function chargerParametrage() {
  param = await api("/api/parametrage");
  for (const k of ["operations", "journaux", "evenements", "rubriques", "comptes", "mappings", "recettes"]) {
    $("#n-" + k).textContent = param[k].length;
  }
  remplirListes();
  afficherListes();
  afficherRecettes();
}

function remplirListes() {
  const sources = {
    operations: param.operations.map((o) => [o.code, `${o.code} · ${o.libelle ?? ""}`]),
    journaux: param.journaux.map((j) => [j.code, `${j.code} · ${j.libelle ?? ""}`]),
    evenements: param.evenements.map((e) => [e.code, `${e.code} · ${e.libelle ?? ""}`]),
    rubriques: param.rubriques.map((r) => [r.code, `${r.code} · ${r.libelle ?? ""}`]),
    comptes: param.comptes.map((c) => [c.code, `${c.code} · ${c.libelle ?? ""}`]),
  };
  $$("select[data-liste]").forEach((s) => {
    const choisi = s.value;
    s.replaceChildren(...sources[s.dataset.liste].map(([v, t]) => new Option(t, v)));
    if (choisi && sources[s.dataset.liste].some(([v]) => v === choisi)) s.value = choisi;
  });
}

function boutonSupprimer(chemin, nom) {
  return el("button", {
    type: "button", class: "supprimer", "aria-label": `Supprimer ${nom}`,
    onclick: async () => {
      if (!confirm(`Supprimer ${nom} ? Il disparaîtra des listes, mais reste dans l'historique.`)) return;
      try { await api(chemin, { method: "DELETE" }); message(`${nom} supprimé.`); await chargerParametrage(); }
      catch (e) { message(e.message, false); }
    },
  }, "Supprimer");
}

function remplirTableau(id, lignes, colonnes, vide) {
  const corps = $("#" + id);
  corps.replaceChildren();
  if (!lignes.length) {
    corps.append(el("tr", {}, el("td", { colspan: String(colonnes + 1), class: "vide-ligne" }, vide)));
    return;
  }
  lignes.forEach((cellules) => corps.append(el("tr", {}, cellules.map((c) => el("td", {}, c)))));
}

function afficherListes() {
  const natures = { ACTIF: "Actif", CHARGE: "Charge", PASSIF: "Passif", PRODUIT: "Produit" };
  remplirTableau("t-operations", param.operations.map((o) => [o.code, o.libelle, o.createdBy,
    boutonSupprimer(`/api/parametrage/operations/${encodeURIComponent(o.code)}`, `l'opération ${o.code}`)]), 3,
    "Aucune opération. Ajoutez-en une avec le formulaire.");
  remplirTableau("t-journaux", param.journaux.map((j) => [j.code, j.libelle, j.codeExterne,
    boutonSupprimer(`/api/parametrage/journaux/${encodeURIComponent(j.code)}`, `le journal ${j.code}`)]), 3,
    "Aucun journal pour l'instant.");
  remplirTableau("t-evenements", param.evenements.map((e) => [e.code, e.libelle, e.operationCode, e.journalCode,
    boutonSupprimer(`/api/parametrage/evenements/${encodeURIComponent(e.code)}`, `l'événement ${e.code}`)]), 4,
    "Aucun événement. Il faut d'abord une opération et un journal.");
  remplirTableau("t-rubriques", param.rubriques.map((r) => [r.code, r.libelle, el("span", { class: "nature" }, natures[r.nature]),
    boutonSupprimer(`/api/parametrage/rubriques/${encodeURIComponent(r.code)}`, `la rubrique ${r.code}`)]), 3,
    "Aucune rubrique pour l'instant.");
  remplirTableau("t-comptes", param.comptes.map((c) => [c.code, c.libelle, c.createdBy,
    boutonSupprimer(`/api/parametrage/comptes/${encodeURIComponent(c.code)}`, `le compte ${c.code}`)]), 3,
    "Aucun compte pour l'instant.");
  remplirTableau("t-mappings", param.mappings.map((m) => [`${m.rubriqueCode} · ${libelleRubrique(m.rubriqueCode)}`,
    m.classeActif === "*" ? "toutes" : m.classeActif, m.categorie === "*" ? "toutes" : m.categorie, m.compteCode,
    boutonSupprimer(`/api/parametrage/mappings/${m.id}`, `la ligne ${m.rubriqueCode} → ${m.compteCode}`)]), 4,
    "Aucune ligne de mapping. Sans elle, le moteur ne sait pas dans quel compte écrire.");
}

function afficherRecettes() {
  const zone = $("#t-recettes");
  zone.replaceChildren();
  if (!param.recettes.length) {
    zone.append(el("div", { class: "vide" }, "Aucune recette. Sans recette, un événement ne produit pas d'écriture."));
    return;
  }
  param.recettes.forEach((r) => {
    const lignes = el("table", {}, el("tbody", {}, r.lignes.map((l) => el("tr", {},
      el("td", {}, `${l.rubriqueCode} · ${libelleRubrique(l.rubriqueCode)}`),
      el("td", { class: "sens-" + l.sens }, l.sens === "D" ? "Débit" : "Crédit"),
      el("td", {}, l.montantSource),
      el("td", { class: "nature" }, l.ignorerSiZero ? "facultative" : "")))));
    const actions = [];
    if (r.statut === "BROUILLON") actions.push(el("button", { type: "button", class: "petit", onclick: async () => {
      try { await post(`/api/parametrage/recettes/${r.id}/publier`, { utilisateur: utilisateurCourant }); message("Recette publiée."); await chargerParametrage(); }
      catch (e) { message(e.message, false); } } }, "Publier"));
    if (r.statut === "PUBLIEE") actions.push(el("button", { type: "button", class: "supprimer", onclick: async () => {
      if (!confirm("Retirer cette recette ? Elle ne servira plus pour les nouveaux événements. Les écritures déjà créées la gardent.")) return;
      try { await post(`/api/parametrage/recettes/${r.id}/retirer`); message("Recette retirée."); await chargerParametrage(); }
      catch (e) { message(e.message, false); } } }, "Retirer"));
    zone.append(el("div", { class: "carte recette" },
      el("div", { class: "recette-tete" }, el("h3", {}, `${libelleEvenement(r.evenementCode)} · version ${r.version}`), statut(r.statut), ...actions),
      el("div", { class: "nature" }, `Valable à partir du ${new Date(r.dateEffet).toLocaleDateString("fr-FR")}` + (r.publiePar ? ` · publiée par ${r.publiePar}` : "")),
      lignes));
  });
}

function brancher(idForm, chemin, nom) {
  $("#" + idForm).onsubmit = async (ev) => {
    ev.preventDefault();
    const donnees = Object.fromEntries(new FormData(ev.target).entries());
    try {
      await post(chemin, donnees);
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

// Recette : lignes dynamiques + aperçu en direct
function ajouterLigneRecette(sensParDefaut = "D") {
  const rub = el("select", { "aria-label": "Rubrique" }, param.rubriques.map((r) => new Option(`${r.code} · ${r.libelle ?? ""}`, r.code)));
  const sens = el("select", { "aria-label": "Côté" }, new Option("Débit · où va la valeur", "D"), new Option("Crédit · d'où elle part", "C"));
  const source = el("input", { "aria-label": "Montant reçu à utiliser", placeholder: "nominal", required: "" });
  sens.value = sensParDefaut;
  const fac = el("input", { type: "checkbox", "aria-label": "Ligne facultative" });
  const tr = el("tr", {}, el("td", {}, rub), el("td", {}, sens), el("td", {}, source), el("td", {}, fac),
    el("td", {}, el("button", { type: "button", class: "supprimer", "aria-label": "Retirer la ligne", onclick: () => { tr.remove(); apercuRecette(); } }, "Retirer")));
  [rub, sens, source, fac].forEach((x) => x.addEventListener("input", apercuRecette));
  $("#lignes-recette").append(tr);
  apercuRecette();
}
function lignesSaisies() {
  return $$("#lignes-recette tr").map((tr) => {
    const [rub, sens, source, fac] = $$("select, input", tr);
    return { rubrique: rub.value, sens: sens.value, montantSource: source.value.trim(), ignorerSiZero: fac.checked };
  });
}
function apercuRecette() {
  const lignes = lignesSaisies();
  const zone = $("#apercu-recette");
  const aDebit = lignes.some((l) => l.sens === "D");
  const aCredit = lignes.some((l) => l.sens === "C");
  zone.replaceChildren(
    aDebit && aCredit
      ? el("span", { class: "ok" }, "La recette a au moins un débit et un crédit.")
      : el("span", { class: "ko" }, "Il faut au moins une ligne au débit et une au crédit."),
    el("ul", {}, lignes.map((l) => el("li", {},
      `${l.sens === "D" ? "Débit" : "Crédit"} ${libelleRubrique(l.rubrique)} avec le montant « ${l.montantSource || "…"} »${l.ignorerSiZero ? " (si présent)" : ""}`))));
}
$("#ajouter-ligne").onclick = () => ajouterLigneRecette();
$("#f-recettes").onsubmit = async (ev) => {
  ev.preventDefault();
  const f = ev.target;
  try {
    const r = await post("/api/parametrage/recettes", {
      evenementCode: f.evenementCode.value, dateEffet: f.dateEffet.value || null,
      utilisateur: utilisateurCourant, publier: f.publier.checked, lignes: lignesSaisies(),
    });
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
function recetteEnVigueur(code) {
  return param.recettes.filter((r) => r.evenementCode === code && r.statut === "PUBLIEE").sort((a, b) => b.version - a.version)[0];
}
async function preparerGeneration() {
  if (!param) await chargerParametrage();
  const sel = $("#g-evenement");
  const choisi = sel.value;
  const prets = param.evenements.filter((e) => recetteEnVigueur(e.code));
  sel.replaceChildren(...prets.map((e) => new Option(`${e.libelle ?? e.code}`, e.code)));
  if (choisi && prets.some((e) => e.code === choisi)) sel.value = choisi;
  if (!$("#g-date").value) $("#g-date").value = new Date().toISOString().slice(0, 10);
  nouvelIdentifiant();
  champsMontants();
}
function nouvelIdentifiant() {
  $("#g-eventId").value = `${$("#g-evenement").value || "EVT"}-${Date.now().toString(36).toUpperCase()}`;
}
function champsMontants() {
  const zone = $("#g-montants");
  zone.replaceChildren();
  const r = recetteEnVigueur($("#g-evenement").value);
  if (!r) { zone.append(el("div", { class: "vide" }, "Aucun événement n'a de recette publiée. Créez-en une à l'étape 1.")); return; }
  const noms = new Map();
  r.lignes.forEach((l) => noms.set(l.montantSource, (noms.get(l.montantSource) ?? true) && l.ignorerSiZero));
  noms.forEach((facultatif, nom) => zone.append(el("label", {}, nom.replaceAll("_", " "),
    facultatif ? el("span", { class: "facultatif" }, "facultatif") : "",
    el("input", { type: "number", step: "0.01", min: "0", inputmode: "decimal", name: nom, ...(facultatif ? {} : { required: "" }) }))));
}
$("#g-evenement").onchange = () => { nouvelIdentifiant(); champsMontants(); $("#g-resultat").replaceChildren(); };
$("#g-exemple").onclick = () => {
  const ex = EXEMPLES[$("#g-evenement").value] || {};
  $$("#g-montants input").forEach((i) => { i.value = ex[i.name] ?? (i.required ? 1000 : ""); });
};
$("#f-generer").onsubmit = async (ev) => {
  ev.preventDefault();
  const f = ev.target;
  const montants = {};
  $$("#g-montants input").forEach((i) => { if (i.value !== "") montants[i.name] = Number(i.value); });
  const zone = $("#g-resultat");
  zone.replaceChildren();
  try {
    const r = await post("/api/evenements", {
      source: "PAGE_TEST", eventId: f.eventId.value, evenement: f.evenement.value,
      dateValeur: f.dateValeur.value, classeActif: f.classeActif.value, categorie: f.categorie.value,
      referenceTitre: f.referenceTitre.value || null, utilisateur: utilisateurCourant, montants,
    });
    message(r.statut === "DOUBLON" ? "Cet identifiant a déjà été reçu : voici l'écriture existante, aucune nouvelle n'a été créée." : "Écriture générée. Elle attend sa validation.");
    afficherPiece(r.ecriture, zone);
    if (r.statut !== "DOUBLON") nouvelIdentifiant();
  } catch (e) {
    zone.append(el("div", { class: "refus" }, el("b", {}, "Le moteur a refusé cet événement"), e.message));
  }
};

// =================== 3. VALIDER ===================
function afficherPiece(e, conteneur) {
  if (conteneur.id === "detail-ecriture") pieceAffichee = e;
  const lignes = e.lignes.map((l) => el("tr", {},
    el("td", {}, `${l.rubrique} · ${libelleRubrique(l.rubrique)}`),
    el("td", {}, l.compte),
    el("td", { class: "num col-debit" }, montant(l.debit)),
    el("td", { class: "num col-credit" }, montant(l.credit))));
  const tableau = el("table", {},
    el("thead", {}, el("tr", {}, el("th", {}, "Rubrique"), el("th", {}, "Compte"), el("th", { class: "num col-debit" }, "Débit"), el("th", { class: "num col-credit" }, "Crédit"))),
    el("tbody", {}, lignes),
    el("tfoot", {}, el("tr", {}, el("td", {}, "Total"), el("td", {}), el("td", { class: "num col-debit" }, montant(e.totalDebit)), el("td", { class: "num col-credit" }, montant(e.totalCredit)))));
  const balance = el("div", { class: "balance " + (e.equilibree ? "ok" : "ko") },
    el("strong", {}, e.equilibree ? "Équilibrée : débit = crédit" : "Déséquilibrée"),
    `${montant(e.totalDebit)} = ${montant(e.totalCredit)} ${"XOF"}`);
  const actions = el("div", { class: "piece-actions" });
  if (e.statut === "PROPOSEE") {
    if (e.creePar === utilisateurCourant) {
      actions.append(el("span", {}, "Vous avez créé cette écriture. Pour la valider, passez en « Comptable » en haut de la page."));
    }
    actions.append(el("button", { type: "button", onclick: async () => {
      try {
        const v = await post(`/api/ecritures/${e.id}/valider`, { utilisateur: utilisateurCourant });
        message(`Écriture validée sous le numéro ${v.numero}.`);
        afficherPiece(v, conteneur);
        if (!$("#ecritures").hidden) chargerEcritures(v.id);
      } catch (err) { message(err.message, false); }
    } }, `Valider en tant que ${utilisateurCourant}`));
  } else if (e.statut === "VALIDEE") {
    actions.append(el("span", {}, "Validée : cette écriture ne peut plus changer. Une erreur se corrige par une écriture inverse."));
  }
  conteneur.replaceChildren(el("article", { class: "piece" },
    el("div", { class: "piece-tete" },
      el("div", {},
        el("div", { class: "piece-numero" + (e.numero ? "" : " brouillon") }, e.numero || "Pas encore numérotée"),
        el("div", { class: "piece-info" }, `${libelleEvenement(e.evenement)} · journal ${e.journal} · le ${new Date(e.dateValeur).toLocaleDateString("fr-FR")}`),
        el("div", { class: "piece-info" }, `Recette version ${e.recetteVersion} · créée par ${e.creePar}` + (e.valideePar ? ` · validée par ${e.valideePar}` : ""))),
      statut(e.statut)),
    tableau, balance, actions));
}

async function chargerEcritures(idChoisi) {
  if (!param) await chargerParametrage();
  let liste;
  try { liste = await api("/api/ecritures"); } catch (e) { message(e.message, false); return; }
  const corps = $("#t-ecritures");
  corps.replaceChildren();
  if (!liste.length) {
    corps.append(el("tr", {}, el("td", { colspan: "5" }, "Aucune écriture. Générez-en une à l'étape 2.")));
    return;
  }
  liste.forEach((e) => {
    const tr = el("tr", { class: "clic", tabindex: "0" },
      el("td", {}, e.numero || "à numéroter"), el("td", {}, libelleEvenement(e.evenement)), el("td", {}, statut(e.statut)),
      el("td", { class: "num" }, montant(e.totalDebit)), el("td", { class: "num" }, montant(e.totalCredit)));
    const ouvrir = () => { $$("#t-ecritures tr").forEach((x) => x.classList.remove("choisi")); tr.classList.add("choisi"); afficherPiece(e, $("#detail-ecriture")); };
    tr.onclick = ouvrir;
    tr.onkeydown = (k) => { if (k.key === "Enter") ouvrir(); };
    corps.append(tr);
    if (idChoisi === e.id) ouvrir();
  });
}

// ---------- Démarrage ----------
chargerParametrage()
  .then(() => { ajouterLigneRecette("D"); ajouterLigneRecette("C"); })
  .catch((e) => message(e.message, false));
