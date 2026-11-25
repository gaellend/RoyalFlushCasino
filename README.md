# Royal Flush Casino

Application Android de casino réalisée dans le cadre du cours de développement mobile.
Elle utilise l'API gratuite [Deck of Cards](https://deckofcardsapi.com/) pour simuler de vrais paquets de cartes.

Sur la page d'accueil, on choisit son jeu et on consulte le podium. On joue au Blackjack contre le croupier avec des jetons, et les joueurs, leurs soldes et leurs mains sont enregistrés sur le téléphone. Un classement permet de comparer les joueurs.


## Fonctionnalités

### Accueil (le hall)
- Les jeux disponibles, présentés avec des cartes chargées depuis l'API. Le Vidéo Poker est affiché comme « Bientôt » : il n'est pas encore développé.
- Le podium des 3 meilleurs joueurs.

### Choix du joueur
- Création d'un profil avec un pseudo de 15 caractères maximum. Deux pseudos qui ne diffèrent que par les accents ou les majuscules sont considérés comme identiques.
- Liste des joueurs avec leur solde et leur record.
- Suppression d'un joueur, après confirmation.
- Un joueur ruiné peut relancer son profil avec 1000 jetons : son record est conservé.

### Blackjack
- Mise avec des jetons de 10, 50, 100 et 500.
- Sabot de 6 paquets mélangés, re-mélangé quand il reste moins de 60 cartes.
- Règles :
  - le croupier tire jusqu'à 17 ;
  - un blackjack paie 3 pour 2 ;
  - possibilité de **doubler** ;
  - **séparer** une paire, une seule fois (les as séparés ne reçoivent qu'une carte chacun) ;
  - **abandonner** en récupérant la moitié de la mise.
- Animations de distribution et de retournement des cartes.
- Un joueur dont le solde passe sous la mise minimale (10 jetons) est ruiné et renvoyé à l'accueil.
- Le solde est enregistré dès qu'une mise est posée, et chaque main est sauvegardée dans l'historique.

### Classement
- Podium des 3 premiers, puis le reste des joueurs, triés par record.
- Fiche détaillée d'un joueur :
  - mains jouées, victoires, blackjacks, plus gros gain ;
  - les 20 dernières mains.

### Adaptation à l'écran
- La table de Blackjack a une mise en page dédiée au mode paysage.
- Le classement utilise une mise en page liste / détails : sur téléphone, la fiche s'ouvre à la place de la liste ; sur tablette, les deux s'affichent côte à côte.
- L'accueil utilise une grille adaptative.

## Utilisation de l'API Deck of Cards

| Requête | Utilisation dans l'app |
|---|---|
| `GET /api/deck/new/shuffle/?deck_count=6` | Créer le sabot de 6 paquets mélangés au début d'une partie |
| `GET /api/deck/{deck_id}/draw/?count=n` | Tirer les cartes du joueur et du croupier |
| `GET /api/deck/{deck_id}/shuffle/` | Remélanger le sabot quand il est presque vide |
| `https://deckofcardsapi.com/static/img/{code}.png` | Images des cartes |

Le nombre de cartes restantes (`remaining`), renvoyé par l'API, sert à décider quand remélanger le sabot.

## Architecture

L'application suit l'architecture vue en cours : **interface → ViewModel → Repository → sources de données**.

```
app/src/main/java/.../
├── data/
│   ├── CardModels.kt          classes @Serializable des réponses de l'API
│   ├── DeckRepository.kt      requêtes Ktor vers Deck of Cards
│   ├── CasinoRepository.kt    accès aux joueurs et aux mains (Room)
│   └── local/                 entités, DAO et base Room
├── game/
│   ├── BlackjackRules.kt      règles du jeu (valeur des mains, résultats, gains)
│   └── Chips.kt               jetons, mise minimale, solde de départ
├── navigation/
│   └── Destinations.kt        destinations de Navigation 3
├── viewmodel/                 un ViewModel par écran
└── ui/
    ├── screens/               les écrans (accueil, joueurs, table, classement)
    ├── components/            cartes, jetons, sabot, boutons
    └── theme/                 couleurs, polices, thème
```

## Notions du cours utilisées

| Notion | Où dans l'app |
|---|---|
| Jetpack Compose | Toute l'interface |
| Listes Lazy | Liste des joueurs, classement, grille de l'accueil (`LazyVerticalGrid`) |
| Navigation 3 | `NavDisplay` et une pile de destinations ; la table reçoit l'id du joueur en argument |
| Layouts responsive | Classes de taille de fenêtre, mise en page paysage, `ListDetailPaneScaffold` |
| ViewModel et StateFlow | Un ViewModel par écran, état exposé en `StateFlow` |
| Coroutines et Flows | Requêtes dans `viewModelScope`, distribution des cartes en `Flow`, données Room observées en `Flow` |
| Ktor | `HttpClient(CIO)`, `ContentNegotiation` pour le JSON, `Logging` |
| Room | Tables `players` et `hands`, DAO, clé étrangère, statistiques calculées en SQL |

## Technologies

- Kotlin 2.4.0, Jetpack Compose (Material 3)
- Navigation 3 (1.1.7)
- Ktor Client 3.5.2 (moteur CIO) et kotlinx.serialization
- Coil 3.5.0 pour le chargement des images
- Room 2.8.5 avec KSP
- Material 3 Adaptive (1.2.0)

## Lancer le projet

Ouvrir le projet dans Android Studio, attendre la synchronisation Gradle, puis lancer l'app sur un émulateur ou un téléphone. Une connexion Internet est nécessaire pour tirer les cartes.
