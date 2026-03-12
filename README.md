# SCP Gate Mod — Forge 1.12.2

## Descrizione
Aggiunge il **cancello SCP** (come nei video/giochi SCP) come blocco multiblock:
- **4 blocchi largo** (due pannelli da 2 blocchi ciascuno)
- **3 blocchi alto**
- **Mezzo blocco profondo** (0.5), centrato nella cella (da 0.25 a 0.75)
- Si apre **solo tramite segnale Redstone** (non con il click)
- Animazione: i pannelli **scorrono lateralmente** (sinistra e destra)
- Suoni: apertura e chiusura con i tuoi file audio personalizzati

---

## Come compilare

### Requisiti
- **Java 8 JDK**
- **Forge MDK 1.12.2** (scarica da https://files.minecraftforge.net versione 14.23.5.2860)

### Passi
```bash
# 1. Scarica e decomprimi il Forge MDK 1.12.2
# 2. Copia il contenuto di questa cartella nella root del MDK (sovrascrivendo src/ e build.gradle)
# 3. Setup Forge (prima volta):
./gradlew setupDecompWorkspace
./gradlew eclipse   # o ./gradlew idea

# 4. Build:
./gradlew build

# Il .jar finito si trova in build/libs/scpgate-1.0.0.jar
# Copia il .jar nella cartella mods/ di Minecraft
```

---

## Come usare in gioco

### Piazzare il cancello
1. Trova l'**SCP Gate** nella scheda creativa "Building Blocks"
2. **Fai clic destro** sul blocco in basso a sinistra dove vuoi il cancello
   - La porta si orienta nella direzione in cui stai guardando
   - Si espande **4 blocchi in larghezza** e **3 blocchi in altezza** a partire dal punto cliccato
   - Assicurati che ci sia spazio libero (4×3), altrimenti non viene piazzata
3. Un messaggio di errore appare se lo spazio non è sufficiente

### Aprire/chiudere
- Metti una **leva** o un **pulsante** (o qualsiasi sorgente di Redstone) **adiacente a qualsiasi blocco** del cancello (master o frame)
- Con leva: **rimane aperto** finché la leva è attiva; si richiude automaticamente quando viene disattivata
- Con pulsante: apre per il tempo del segnale, poi si richiude

### Distruggere
- Rompi **qualsiasi parte** del cancello: tutta la struttura 4×3 viene rimossa e droppato **1 item** del cancello

---

## Struttura dei file
```
src/main/java/com/scpmod/scpgate/
├── SCPGateMod.java              ← Classe principale @Mod
├── proxy/
│   ├── CommonProxy.java         ← Proxy server
│   └── ClientProxy.java         ← Proxy client (TESR, modelli)
├── blocks/
│   ├── BlockSCPGate.java        ← Blocco master (pos 0,0 in basso a sinistra)
│   └── BlockSCPGateFrame.java   ← Blocco frame (gli altri 11 slot)
├── tileentity/
│   ├── TileEntitySCPGate.java   ← Stato porta (powered, animazione)
│   └── TileEntitySCPGateFrame.java ← Riferimento al master
├── renderer/
│   └── TESRSCPGate.java         ← Rendering OpenGL animato
└── init/
    ├── ModBlocks.java           ← Registrazione blocchi
    └── ModSounds.java           ← Registrazione suoni

src/main/resources/assets/scpgate/
├── sounds/
│   ├── bigdooropen1.ogg         ← Suono apertura
│   └── bigdoorclose2.ogg        ← Suono chiusura
├── sounds.json
├── textures/blocks/
│   ├── scp_gate_panel.png       ← Texture pannelli (generata automaticamente)
│   └── scp_gate_side.png        ← Texture bordi laterali
├── blockstates/                 ← Stato blocchi per 4 orientamenti
├── models/                      ← Modelli (l'inventario usa il modello 2D)
└── lang/en_us.lang
```

---

## Personalizzare la texture
Sostituisci `src/main/resources/assets/scpgate/textures/blocks/scp_gate_panel.png`  
con la tua immagine **128×192 px** (4 blocchi = 128px, 3 blocchi = 192px, 32px per blocco).
- La **metà sinistra** (0–63px) è il pannello sinistro
- La **metà destra** (64–127px) è il pannello destro

---

## Note tecniche
- Il **blocco master** è il TESR (TileEntitySpecialRenderer) che renderizza l'intera struttura 4×3
- I **frame block** sono invisibili (INVISIBLE render type) ma hanno la hitbox di mezzo blocco
- La collisione si disabilita automaticamente non appena il cancello inizia ad aprirsi
- Il segnale Redstone viene rilevato su **qualsiasi** dei 12 blocchi della struttura
