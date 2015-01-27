# Konkurentné programovanie (ÚINF/KOPR/2014)

## Ako na to?
V `sk.upjs.kopr2014.ppatrik.common.Config` si nastavme informacie servera a teda subor port a jeho IP.
Nasledne spustit server `Server` (v pripade ze nechceme pustat server separatne tak pouzime spustac `ClientServer`).
Ak server nabehol spustime clienta `Client`.
Tlacidla:
- Štart - spusta stahovanie zo servera od posledneho stavu
- Pause - pozastavi stahovanie - program moze byt zavrety (aktualny stav je ulozeny aj ked sa program zavrie bez prerusenia)
- Stop - ukonci stahovanie zmaze subory a zatvori sa

(c) 2015 Patrik Pekarčík

## Zadanie

### Vytvorte klient-server aplikáciu na kopírovanie súboru. Požadované vlastnosti:

Sťahovanie súboru prebieha paralelne cez používateľom daný počet TCP soketov.

Toto sťahovanie je prerušiteľné tak, že ho je možné prerušiť, vypnúť program, napr. sa presunúť na iné miesto na internete a pri opätovnom pokuse o skopírovanie toto kopírovanie pokračuje od momentu prerušenia opäť paralelne cez rovnaký počet TCP soketov.

Grafické používateľské rozhranie obsahujúce aspoň:
- progressbar znázorňujúci percento skopírovania celého súboru
- tlačidlo na prerušenie kopírovania a následné vypnutie programu
- tlačidlo na opätovné pokračovanie v kopírovaní, ak je (pri spustení) zistené, že kopírovanie bolo prerušené
- tlačidlo na úplné zrušenie kopírovania bez možnosti pokračovania, ktoré nezanechá na disku žiadnu stopu

Môžete si vybrať, či bude prebiehať kopírovanie zo servra na klienta alebo naopak, nie je potrebné programovať oba smery

Stačí uvažovať kopírovanie jedného súboru v rovnakom čase

Nie je potrebné programovať prehľadávač disku "na druhej strane" na výber súboru

Odporúčam nepoužívať vytváranie špeciálnych paketov s hlavičkami, ale posielať cez Socket.getOutputStream().write() po prípadných úvodných dohodách iba dáta. Uzatvorenie streamu sa dá odchytiť cez výnikmku IOException, keď sa zatvorí socket.

Program má byť schopný skopírovať bez problémov na lokálnej sieti, alebo v rámci localhostu aj 1GB súbor pod 1 minútu