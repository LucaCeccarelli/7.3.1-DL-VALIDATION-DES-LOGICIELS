> CECCARELLI Luca <br>
> LADRETTE Irwin
# TDD - Bibliotheque

## Comment le TDD a ete respecte
- Les tests definissent les scenarios (S1 a S12) avant l'implementation.
- Les stubs Mockito (`when(...)`) ont ete retires pour que les tests exercent le vrai code.
- L'implementation a ensuite ete ecrite pour satisfaire chaque assertion et exception attendue.
- Chaque echec a ete corrige en ajustant la logique, puis re-verifie avec `mvn test`.

## Differences entre la version initiale et la version implementee
### Version initiale (avec mocks et "Non implemente")
- `TestBibliotheque` utilisait des mocks et des `when(...)` pour simuler tous les retours.
- Les methodes de `Abonne` et `Bibliotheque` levaient `UnsupportedOperationException("Non implemente")`.
- Le code metier n'etait pas execute par les tests.

### Comment les tests ont ete ecrits a partir de l'enonce
Les tests ont ete deduits directement des scenarios S1 a S12. Chaque test cree les donnees citees (abonnes, livres, dates) puis verifie l'effet attendu (exception, liste vide, retour d'emprunt, reservation, etc.). Conformement aux consignes, la version "tests uniquement" utilisait Mockito pour simuler les methodes de `Bibliotheque` et `Abonne` tant que l'implementation etait absente. Les elements ajoutes au code (ex. `Livre`, `Exemplaire`, `Emprunt`, `EmpruntDecision`, enums de resultats, exceptions) justifient la modelisation necessaire des operations du cahier des charges: un ISBN + numero d'exemplaire, la date de retour prevue, l'etat d'un retour, la file d'attente des reservations et les erreurs attendues (abonne/ouvrage inconnu).

### Version implementee (sans stubs)
- Les tests utilisent des objets reels (`new Bibliotheque()`, `new Abonne(...)`).
- `Abonne` expose ses champs via des getters fonctionnels.
- `Bibliotheque` contient une logique qui permet de satisfaire les scenarios (identification, recherche, reservation, emprunt, retour).

## Couverture de code
Commande utilisee:
```
mvn test
```

Resultat (JaCoCo, couverture d'instructions): 94.77%

Le rapport est genere dans `target/site/jacoco/`.
