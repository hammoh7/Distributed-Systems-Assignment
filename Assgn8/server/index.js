const WebSocket = require("ws");
const { v4: uuidv4 } = require("uuid");
const wss = new WebSocket.Server({ port: 8080 });

const gameState = {
  players: new Map(),
  bullets: new Map(),
  lastBulletId: 0,
};

const PLAYER_SPEED = 3;
const BULLET_SPEED = 10;
const MAP_WIDTH = 1600;
const MAP_HEIGHT = 1200;

wss.on("connection", (ws) => {
  const playerId = uuidv4();

  gameState.players.set(playerId, {
    x: Math.random() * MAP_WIDTH,
    y: Math.random() * MAP_HEIGHT,
    angle: 0,
    health: 100,
    color: `hsl(${Math.random() * 360}, 100%, 50%)`,
  });

  ws.send(
    JSON.stringify({
      type: "init",
      playerId,
      state: Array.from(gameState.players.entries()),
    })
  );

  ws.on("message", (data) => {
    const message = JSON.parse(data);
    handleClientMessage(playerId, message);
  });

  ws.on("close", () => {
    gameState.players.delete(playerId);
    gameState.bullets.forEach((bullet, id) => {
      if (bullet.owner === playerId) {
        gameState.bullets.delete(id);
      }
    });
    broadcastState();
  });
});

function handleClientMessage(playerId, message) {
  const player = gameState.players.get(playerId);
  if (!player) return;

  switch (message.type) {
    case "movement":
      updatePlayerPosition(player, message);
      break;
    case "shoot":
      createBullet(player, playerId);
      break;
  }
}

function updatePlayerPosition(player, { angle }) {
  player.angle = angle;
  const dx = Math.cos(angle) * PLAYER_SPEED;
  const dy = Math.sin(angle) * PLAYER_SPEED;
  player.x = Math.max(0, Math.min(MAP_WIDTH, player.x + dx));
  player.y = Math.max(0, Math.min(MAP_HEIGHT, player.y + dy));
}

function createBullet(player, playerId) {
  const bulletId = ++gameState.lastBulletId;
  gameState.bullets.set(bulletId, {
    x: player.x + Math.cos(player.angle) * 20,
    y: player.y + Math.sin(player.angle) * 20,
    angle: player.angle,
    owner: playerId,
  });
}

function updateBullets() {
  gameState.bullets.forEach((bullet, id) => {
    bullet.x += Math.cos(bullet.angle) * BULLET_SPEED;
    bullet.y += Math.sin(bullet.angle) * BULLET_SPEED;
    if (
      bullet.x < 0 ||
      bullet.x > MAP_WIDTH ||
      bullet.y < 0 ||
      bullet.y > MAP_HEIGHT
    ) {
      gameState.bullets.delete(id);
    }
  });
}

function checkCollisions() {
  gameState.bullets.forEach((bullet, bulletId) => {
    gameState.players.forEach((player, playerId) => {
      if (
        playerId !== bullet.owner &&
        distance(bullet.x, bullet.y, player.x, player.y) < 20
      ) {
        player.health -= 10;
        gameState.bullets.delete(bulletId);
        if (player.health <= 0) {
          gameState.players.delete(playerId);
        }
      }
    });
  });
}

function broadcastState() {
  const state = {
    players: Array.from(gameState.players.entries()),
    bullets: Array.from(gameState.bullets.entries()),
  };
  wss.clients.forEach((client) => {
    if (client.readyState === WebSocket.OPEN) {
      client.send(JSON.stringify(state));
    }
  });
}

function distance(x1, y1, x2, y2) {
  return Math.sqrt((x2 - x1) ** 2 + (y2 - y1) ** 2);
}

setInterval(() => {
  updateBullets();
  checkCollisions();
  broadcastState();
}, 1000 / 60);

console.log("Game server running on ws://localhost:8080");