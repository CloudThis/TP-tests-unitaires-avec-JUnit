# TP – Tests unitaires avec JUnit

## 1. Présentation

Ce projet implémente une petite application de **gestion de comptes bancaires** en Java, testée avec **JUnit 5**.

Deux classes métier composent le cœur du projet :

- `CompteBancaire` : représente un compte (IBAN, titulaire, solde, découvert autorisé). Il permet de déposer, retirer et calculer des intérêts sur le solde.
- `GestionnaireComptes` : gère une collection de comptes (indexés par IBAN). Il permet d'ajouter un compte, d'en rechercher un, d'effectuer un virement entre deux comptes, de calculer le solde total et de lister les comptes en découvert.

Les erreurs métier (montant invalide, solde insuffisant, compte inconnu, compte déjà existant) sont représentées par des exceptions dédiées, situées dans le package `Main.banque.exceptions`.

## 2. Choix de conception

**Découpage des classes.** Le projet sépare volontairement :
- la **logique d'un compte unique** (`CompteBancaire`, qui ne connaît que son propre solde),
- de la **logique de gestion d'un ensemble de comptes** (`GestionnaireComptes`, qui orchestre plusieurs `CompteBancaire`, par exemple pour un virement).

Cela respecte le principe de responsabilité unique : `CompteBancaire` ne sait pas qu'il existe d'autres comptes, et `GestionnaireComptes` ne connaît pas les règles internes d'un compte (il se contente d'appeler `deposer`/`retirer`). Les exceptions sont regroupées dans un sous-package `exceptions` pour ne pas polluer le code métier et pour pouvoir être réutilisées par les deux classes.

**Gestion des exceptions.** Toutes les exceptions métier (`MontantInvalideException`, `SoldeInsuffisantException`, `CompteInconnuException`, `CompteDejaExistantException`) héritent de `RuntimeException` (exceptions non vérifiées). Ce choix a été fait pour ne pas alourdir les signatures des méthodes avec des `throws` en cascade, ce qui aurait été particulièrement gênant dans `GestionnaireComptes.virement()`, qui appelle successivement plusieurs méthodes pouvant chacune échouer pour des raisons différentes.

Les erreurs de programmation (arguments `null`, IBAN vide, découvert négatif) restent, elles, signalées par `IllegalArgumentException`, standard du JDK, pour bien distinguer :
- une erreur d'utilisation de l'API (`IllegalArgumentException`),
- une erreur "métier" attendue dans le déroulement normal du programme (nos exceptions dédiées).

Le point le plus délicat est la méthode `virement()` : un virement enchaîne un retrait puis un dépôt. Pour éviter qu'un retrait réussi ne soit "perdu" si le dépôt échoue (ce qui romprait l'équilibre des comptes), le retrait est effectué en premier (il est donc vérifié avant tout effet de bord), et un `try/catch` autour du dépôt permet d'annuler le retrait si jamais un problème survient malgré tout, afin de garantir l'atomicité de l'opération.

les classes ont été écrites en premier (`CompteBancaire`, puis `GestionnaireComptes`), suivies de leurs tests. Ce n'est donc pas du TDD strict (tests écrits avant le code), mais une démarche de **tests après coup**.

## 3. Comment lancer les tests

Le projet utilise Maven (voir `pom.xml`, avec JUnit 5.10.2 et le plugin Surefire). Depuis la racine du projet :

```bash
mvn test
```

## 4. Récapitulatif des tests

| Classe de test | Nombre de tests | Ce qu'ils couvrent |
|---|---|---|
| `CompteBancaireTest` | 13 | **Cas nominaux (3)** : dépôt qui augmente le solde, retrait qui le diminue, calcul des intérêts sur un solde positif.<br>**Cas limites (4)** : retrait amenant exactement au découvert autorisé (accepté), retrait d'un centime de plus (refusé), dépôt et retrait de montant nul (`MontantInvalideException`).<br>**Cas d'erreur (6)** : dépôt/retrait négatifs, retrait dépassant largement le découvert, taux d'intérêt négatif, intérêts nuls sur solde ≤ 0, cohérence de `estEnDecouvert()`. |
| `GestionnaireComptesTest` | 9 | **Cas nominaux (3)** : recherche d'un compte par IBAN, virement réussi qui met à jour les deux soldes, calcul du solde total.<br>**Cas limites (2)** : filtrage des comptes en découvert, virement du montant exact du solde (compte vidé sans passer en négatif).<br>**Cas d'erreur (4)** : IBAN inconnu, IBAN déjà existant, virement qui échoue par solde insuffisant (aucun solde modifié), virement vers un IBAN inconnu (aucun solde modifié). |
| **Total** | **22** | |

## 5. Difficultés rencontrées

- **Distinguer "montant invalide" de "solde insuffisant".** Au départ, il n'était pas évident de savoir si un retrait de montant négatif devait lever la même exception qu'un retrait dépassant le découvert. Les deux sont des refus de retrait, mais leurs causes sont différentes (erreur de saisie vs. contrainte métier). La solution retenue est de vérifier d'abord la validité du montant (`MontantInvalideException`), puis seulement ensuite la disponibilité du solde (`SoldeInsuffisantException`), ce qui donne un message d'erreur plus précis et plus facile à tester séparément.

- **Le cas limite du découvert autorisé.** Il fallait décider si un retrait amenant le solde *exactement* à `-decouvertAutorise` devait être accepté ou refusé. Le test `retraitJusquauDecouvertExactPasse` fixe ce comportement (accepté), et `retraitUnCentimeDeTropLeveException` vérifie qu'un centime de plus est refusé. Écrire ces deux tests l'un à côté de l'autre a permis de repérer une erreur de comparaison (`<=` au lieu de `<` dans la condition) avant qu'elle ne passe inaperçue.

- **Garantir l'atomicité du virement.** Le premier jet de `virement()` retirait puis déposait sans se soucier d'un échec intermédiaire : si le dépôt échouait après un retrait déjà effectué, le montant "disparaissait" du système. Le test `virementEchoueNeModifieAucunSolde` a mis ce bug en évidence (le solde source restait diminué même quand le virement échouait). La correction a consisté à réordonner les opérations (retrait d'abord, car il est plus susceptible d'échouer) et à ajouter un `try/catch` qui annule le retrait si le dépôt échoue malgré tout.

## 6. Bilan

Ce TP a montré l'intérêt d'organiser les tests par catégories (nominal / limite / erreur) plutôt que de les écrire dans le désordre : cela oblige à se poser des questions tel que "que se passe-t-il quand ça échoue ?", ce qui a permis de trouver un vrai bug (l'atomicité du virement) qui n'était pas visible en testant uniquement le cas normal. Il a aussi mis en évidence l'importance de choisir des exceptions précises et documentées plutôt qu'une exception générique ce qui rend les tests eux-mêmes plus lisibles, puisque `assertThrows` documente directement, dans le test, la raison attendue de l'échec.
