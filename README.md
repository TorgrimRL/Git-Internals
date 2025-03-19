# GitInternals (Kotlin)

GitInternals er et kommandolinjebasert verktøy skrevet i Kotlin som ga meg forståelse av hvordan `.git`-mappen fungerer. Ved å lese og dekomprimere Git-objekter direkte fra disk, lar prosjektet deg utforske commit-historikk, trestrukturer, blob-filer og forgreninger på et lavt nivå – helt uten å bruke eksterne Git-biblioteker.

## Funksjoner

- **`cat-file`**: Viser detaljer om en gitt git-hash, enten det er commit, tree eller blob.  
- **`list-branches`**: Lister alle lokale branches og markerer den aktive branchen med `*`.  
- **`log`**: Traverserer commit-historikken for en oppgitt branch og viser blant annet tidsstempel og commit-meldinger.  
- **`commit-tree`**: Viser filstrukturen (tree-objektet) for en gitt commit.

## Teknologier og Læring

- **Kotlin Data Classes**: Strukturert lagring av informasjon om git-objekter (f.eks. commits og tree-entries).  
- **Extension Functions**: En egendefinert funksjon på `ByteArray` (`indexOfFrom`) for å søke i binære data.  
- **Fil- og Strenghåndtering**: Dekomprimerer Git-objekter og parser headere samt innholdet i objektene.  
- **Ingen eksterne Git-biblioteker**: All logikk for å lese `.git`-mappen er implementert “fra bunnen av” med fil- og strengoperasjoner.
- 
## Mulige Utvidelser

- **Utvidet feilhåndtering**: Mer sikker håndtering av ugyldige eller manglende Git-objekter.  
- **Støtte for flere objekttyper**: For eksempel `tag`-objekter og mer inngående håndtering av reflog.  
- **Automatiserte tester**: Selvskrevne enhetstester for å gjøre prosjektet mer robust og vedlikeholdbart.

---

Dette prosjektet gir praktisk erfaring med filtilgang, strengbehandling og Kotlin-spesifikke funksjonaliteter (data classes, extension functions), og fungerer som en verdifull introduksjon til Git sine indre mekanismer.
