#!/usr/bin/env bash
# Construit et démarre la pile complète : bases, registre, passerelle, microservices et front.
set -euo pipefail

# Délai laissé aux services pour s'inscrire auprès du registre, puis à la passerelle pour
# rafraîchir son catalogue : en deçà, toutes les routes répondent 404.
ATTENTE_INITIALE=60
# Ensuite, nouvelle vérification toutes les 10 secondes, pendant 5 minutes au plus.
INTERVALLE=10
TENTATIVES_MAX=30

# Une route par microservice métier. Inconnue de la passerelle, elle répond 404 ; connue, elle
# atteint le service, qui répond 401 faute d'identifiants : aucun secret n'est donc nécessaire.
ROUTES=(
    "ms-patients/patients"
    "ms-notes/patients/1/notes"
)

docker compose --profile fullstack up -d --build
# Le port du front peut être changé dans .env : on l'y relit pour afficher la bonne adresse.
port_webclient=$(grep -E '^PORT_WEBCLIENT=' .env 2>/dev/null | cut -d= -f2 || true)

# Affiche les routes que la passerelle ne sait pas encore servir ; rien si toutes sont prêtes.
routes_absentes() {
    local route code
    for route in "${ROUTES[@]}"; do
        code=$(curl -s -o /dev/null -w '%{http_code}' --max-time 5 "http://localhost:9000/${route}" || true)
        if [[ "${code}" == "404" || "${code}" == "000" ]]; then
            echo "${route}"
        fi
    done
}

echo "Attente de ${ATTENTE_INITIALE} s : inscription des services auprès du registre…"
sleep "${ATTENTE_INITIALE}"

for ((tentative = 1; ; tentative++)); do
    absentes=$(routes_absentes)
    [[ -z "${absentes}" ]] && break
    if ((tentative > TENTATIVES_MAX)); then
        echo "La passerelle ne route toujours pas vers :" >&2
        echo "${absentes}" | sed 's/^/  /' >&2
        echo "Consulter les journaux : docker compose --profile fullstack logs <service>" >&2
        exit 1
    fi
    echo "Routes pas encore prêtes ($(echo "${absentes}" | wc -l)), nouvel essai dans ${INTERVALLE} s…"
    sleep "${INTERVALLE}"
done

echo "Pile prête :"
echo "  interface web : http://localhost:${port_webclient:-8080}"
echo "  passerelle    : http://localhost:9000"
echo "  registre      : http://localhost:8761"
