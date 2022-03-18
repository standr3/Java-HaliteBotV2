
Modul de functionare: 
	La fiecare "turn" s-a facut verificarea starilor in care se aflau planetele si 
navele, iar in urma dockarii acestora(ship docking) sau a ocuparii planetelor au fost
sterse din vectorii/hashmapurile descrise mai jos.
		
		Vector<Ship> busyShips = new Vector<>();
        /* busyShips => Collection of undocked ships that follow orders. */
        Vector<Ship> shipGarbage = new Vector<>();
        /* shipGarbage => Collection for temporary holding ships that are going to be deleted. */
        Vector<Planet> taskPlanets = new Vector<>();
        /* taskPlanets => Collection of all the planets that can and must be conquered. */
        Vector<Planet> allPlanets = new Vector<>();
        /* allPlanets => Collection of all available planets. */
        HashMap<Ship, Planet> orders = new HashMap<>();
        /* orders => Collection of ships and the planets they must conquer. */

  	Modul de evitare a coliziunilor consta in considerarea unor segmente de lungimea unei diagonale
prin centrul navelor (auxShip in cod) astfel incat cu ajutorul functiei Collision.segmentCircleIntersect 
sa putem verifica daca aceste segemente intersecteaza sfera "ship"-ului curent. In cazul in care coliziunea
"ship"-ului curent ii va fi aplicat un thrust catre o alta directie, in functie de pozitia fata de auxShip, 
adica a navei care vine spre el. Segmentele care se suprapun lui auxShip sunt intre urmatoarele puncte:
	nord-sud
	vest-est
	nordvest-sudest
	nordest-sudvest

	O alta masura care a ajutat la distributia optima a navelor pe harta si evitarea coliziunilor a fost
selectia planetelor dintr-un vector sortat pe baza dinstantei fata de centrul hartii. Acest lucru inseamna 
ca navele vor merge mai intai spre planetele de la marginea hartii si se vor apropia pe masura ce apar nave
noi de centrul hartii.

	Observatie : In fisierul Makefile cu regula check ruleaza checkerul.
	Celelalte clase nu au fost modificate.
