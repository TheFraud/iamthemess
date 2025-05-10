const WebSocket = require('ws');

// Démarrage du serveur WebSocket sur le port 8080
const wss = new WebSocket.Server({ port: 8080 });

// Stockage des utilisateurs connectés (id -> socket)
let clients = {};

wss.on('connection', (ws) => {
    let userId;

    ws.on('message', (message) => {
        const data = JSON.parse(message);

        switch (data.type) {
            case 'register':
                userId = data.id;
                clients[userId] = ws;
                console.log(`Utilisateur enregistré : ${userId}`);
                break;

            case 'offer':
            case 'answer':
            case 'candidate':
                const target = clients[data.target];
                if (target) {
                    target.send(JSON.stringify(data));
                } else {
                    console.log(`Utilisateur cible introuvable : ${data.target}`);
                }
                break;

            default:
                console.log(`Type de message inconnu : ${data.type}`);
        }
    });

    ws.on('close', () => {
        if (userId) {
            delete clients[userId];
            console.log(`Utilisateur déconnecté : ${userId}`);
        }
    });
});

console.log('Serveur WebSocket démarré sur le port 8080');
