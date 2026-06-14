-- ==============================================================================
-- RUNITUP - SCHEMA DATABASE (01-schema.sql)
-- ==============================================================================

-- CREAZIONE TIPI ENUM
CREATE TYPE genere_scarpa AS ENUM ('UOMO', 'DONNA', 'UNISEX');
CREATE TYPE tipo_terreno AS ENUM ('STRADA', 'STERRATO');
CREATE TYPE tipo_passo AS ENUM ('LENTO', 'MEDIO', 'VELOCE');
CREATE TYPE tipo_appoggio AS ENUM ('NEUTRO', 'PRONATORE', 'SUPINATORE');
CREATE TYPE stato_ordine AS ENUM ('IN_LAVORAZIONE', 'SPEDITO', 'CONSEGNATO', 'ANNULLATO');
CREATE TYPE tipo_sconto_coupon AS ENUM ('FISSO', 'PERCENTUALE');
CREATE TYPE metodo_pagamento AS ENUM ('CARTA', 'PAYPAL', 'CONTRASSEGNO');

-- CREAZIONE TABELLE INDIPENDENTI
CREATE TABLE utente (
    id_utente SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    cognome VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    telefono VARCHAR(20),
    is_admin BOOLEAN DEFAULT FALSE
);

CREATE TABLE brand (
    id_brand SERIAL PRIMARY KEY,
    nome_brand VARCHAR(100) UNIQUE NOT NULL
);

CREATE TABLE promozione (
    id_promozione SERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    percentuale DECIMAL(5,2) NOT NULL CHECK (percentuale > 0 AND percentuale <= 100),
    data_inizio TIMESTAMP NOT NULL,
    data_fine TIMESTAMP NOT NULL
);

CREATE TABLE coupon (
    id_coupon SERIAL PRIMARY KEY,
    codice VARCHAR(50) UNIQUE NOT NULL,
    valore_sconto DECIMAL(10,2) NOT NULL CHECK (valore_sconto > 0),
    tipo_sconto tipo_sconto_coupon NOT NULL,
    data_inizio DATE NOT NULL,
    data_scadenza DATE -- Opzionale: se NULL, il coupon non scade mai
);

-- CREAZIONE TABELLE DIPENDENTI E RELAZIONI
CREATE TABLE prodotto (
    id_prodotto SERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    descrizione TEXT,
    id_brand INT NOT NULL REFERENCES brand(id_brand) ON DELETE CASCADE,
    immagine_copertina BYTEA,
    genere genere_scarpa NOT NULL,
    tipo_terreno tipo_terreno NOT NULL,
    tipo_appoggio tipo_appoggio NOT NULL,
    tipo_passo tipo_passo NOT NULL,
    data_inserimento TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    in_evidenza BOOLEAN DEFAULT FALSE,
    totale_vendite INT DEFAULT 0 CHECK (totale_vendite >= 0)
);

CREATE TABLE variante_prodotto (
    id_variante SERIAL PRIMARY KEY,
    id_prodotto INT NOT NULL REFERENCES prodotto(id_prodotto) ON DELETE CASCADE,
    colore VARCHAR(50) NOT NULL,
    taglia VARCHAR(20) NOT NULL,
    prezzo DECIMAL(10,2) NOT NULL CHECK (prezzo >= 0),
    quantita_magazzino INT NOT NULL DEFAULT 0 CHECK (quantita_magazzino >= 0)
);

-- Tabella Ponte N:M per lo storico/programmazione delle Promozioni
CREATE TABLE variante_promozione (
    id_variante INT NOT NULL REFERENCES variante_prodotto(id_variante) ON DELETE CASCADE,
    id_promozione INT NOT NULL REFERENCES promozione(id_promozione) ON DELETE CASCADE,
    PRIMARY KEY (id_variante, id_promozione)
);

CREATE TABLE carrello (
    id_carrello SERIAL PRIMARY KEY,
    id_utente INT UNIQUE NOT NULL REFERENCES utente(id_utente) ON DELETE CASCADE
);

CREATE TABLE voce_carrello (
    id_voce SERIAL PRIMARY KEY,
    id_carrello INT NOT NULL REFERENCES carrello(id_carrello) ON DELETE CASCADE,
    id_variante INT NOT NULL REFERENCES variante_prodotto(id_variante) ON DELETE CASCADE,
    quantita INT NOT NULL DEFAULT 1 CHECK (quantita > 0)
);

CREATE TABLE ordine (
    id_ordine SERIAL PRIMARY KEY,
    id_utente INT NOT NULL REFERENCES utente(id_utente) ON DELETE CASCADE,
    id_coupon INT REFERENCES coupon(id_coupon) ON DELETE SET NULL, -- Se cancelli il coupon, la ricevuta resta!
    data_ordine TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- TOTALI FISCALI
    totale_imponibile DECIMAL(10,2) NOT NULL CHECK (totale_imponibile >= 0),
    totale_iva DECIMAL(10,2) NOT NULL CHECK (totale_iva >= 0),
    totale_pagato DECIMAL(10,2) NOT NULL CHECK (totale_pagato >= 0),
    
    -- DATI DI SPEDIZIONE E PAGAMENTO
    nome_e_cognome_destinatario VARCHAR(200) NOT NULL,
    indirizzo_spedizione TEXT NOT NULL,
    citta VARCHAR(100) NOT NULL,
    cap VARCHAR(10) NOT NULL,
    metodo_pagamento metodo_pagamento NOT NULL,
    stato stato_ordine NOT NULL DEFAULT 'IN_LAVORAZIONE'
);

CREATE TABLE dettaglio_ordine (
    id_dettaglio SERIAL PRIMARY KEY,
    id_ordine INT NOT NULL REFERENCES ordine(id_ordine) ON DELETE CASCADE,
    id_variante INT REFERENCES variante_prodotto(id_variante) ON DELETE SET NULL, -- Può essere NULL se l'admin elimina la scarpa!
    
    -- DATI STORICI (SNAPSHOT)
    nome_prodotto_storico VARCHAR(255) NOT NULL,
    colore_storico VARCHAR(50) NOT NULL,
    taglia_storica VARCHAR(20) NOT NULL,
    
    -- DATI FINANZIARI SINGOLA RIGA
    quantita_acquistata INT NOT NULL CHECK (quantita_acquistata > 0),
    prezzo_unitario_imponibile DECIMAL(10,2) NOT NULL CHECK (prezzo_unitario_imponibile >= 0),
    aliquota_iva DECIMAL(4,2) NOT NULL DEFAULT 22.00,
    prezzo_unitario_con_iva DECIMAL(10,2) NOT NULL CHECK (prezzo_unitario_con_iva >= 0)
);