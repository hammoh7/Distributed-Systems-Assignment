const canvas = document.getElementById("gameCanvas");
const ctx = canvas.getContext("2d");

canvas.width = window.innerWidth;
canvas.height = window.innerHeight;

let playerId = null;
let players = new Map();
let bullets = new Map();
let keys = {};
let mouseX = canvas.width / 2;
let mouseY = canvas.height / 2;

const MAP_WIDTH = 1600;
const MAP_HEIGHT = 1200;
const GRID_SIZE = 50;

const ws = new WebSocket("ws://localhost:8080");

ws.onopen = () => {
  console.log("Connected to server");
};

ws.onerror = (error) => {
  console.error("WebSocket error:", error);
};

canvas.addEventListener("mousemove", (e) => {
  const rect = canvas.getBoundingClientRect();
  mouseX = e.clientX - rect.left;
  mouseY = e.clientY - rect.top;
});

ws.onmessage = (event) => {
  const data = JSON.parse(event.data);
  if (data.type === "init") {
    playerId = data.playerId;
    data.state.forEach(([id, player]) => players.set(id, player));
    return;
  }
  data.players.forEach(([id, player]) => players.set(id, player));
  Array.from(players.keys()).forEach((id) => {
    if (!data.players.find(([pid]) => pid === id)) players.delete(id);
  });
  bullets = new Map(data.bullets);
};

document.addEventListener("keydown", (e) => (keys[e.key] = true));
document.addEventListener("keyup", (e) => (keys[e.key] = false));

function gameLoop() {
  update();
  draw();
  requestAnimationFrame(gameLoop);
}

let lastShotTime = 0;
const SHOOT_COOLDOWN = 200;

function update() {
  const player = players.get(playerId);
  if (!player) return;

  const angle = Math.atan2(
    mouseY - canvas.height / 2,
    mouseX - canvas.width / 2
  );
  ws.send(JSON.stringify({ type: "movement", angle }));

  const now = Date.now();
  if (keys[" "] && now - lastShotTime >= SHOOT_COOLDOWN) {
    ws.send(JSON.stringify({ type: "shoot" }));
    lastShotTime = now;
  }
  document.getElementById("healthValue").textContent = player.health;
}

function draw() {
  ctx.fillStyle = "#111";
  ctx.fillRect(0, 0, canvas.width, canvas.height);

  const localPlayer = players.get(playerId);
  if (!localPlayer) return;

  ctx.save();
  ctx.translate(
    canvas.width / 2 - localPlayer.x,
    canvas.height / 2 - localPlayer.y
  );

  ctx.strokeStyle = "rgba(255, 255, 255, 0.1)";
  ctx.lineWidth = 1;
  for (let x = 0; x <= MAP_WIDTH; x += GRID_SIZE) {
    ctx.beginPath();
    ctx.moveTo(x, 0);
    ctx.lineTo(x, MAP_HEIGHT);
    ctx.stroke();
  }
  for (let y = 0; y <= MAP_HEIGHT; y += GRID_SIZE) {
    ctx.beginPath();
    ctx.moveTo(0, y);
    ctx.lineTo(MAP_WIDTH, y);
    ctx.stroke();
  }

  players.forEach((player, id) => {
    ctx.save();
    ctx.translate(player.x, player.y);
    ctx.rotate(player.angle);

    ctx.fillStyle = player.color;
    ctx.beginPath();
    ctx.moveTo(25, 0);
    ctx.lineTo(-15, 15); 
    ctx.lineTo(-5, 0); 
    ctx.lineTo(-15, -15); 
    ctx.closePath();
    ctx.fill();

    ctx.fillStyle = "rgba(255, 165, 0, 0.5)";
    ctx.beginPath();
    ctx.moveTo(-15, -5);
    ctx.lineTo(-25, 0);
    ctx.lineTo(-15, 5);
    ctx.fill();

    ctx.fillStyle = "#ff0000";
    ctx.fillRect(-20, -30, 40, 5);
    ctx.fillStyle = "#00ff00";
    ctx.fillRect(-20, -30, 40 * (player.health / 100), 5);

    ctx.restore();
  });

  bullets.forEach((bullet) => {
    ctx.fillStyle = "#ffff00";
    ctx.beginPath();
    ctx.arc(bullet.x, bullet.y, 4, 0, Math.PI * 2);
    ctx.fill();
    ctx.strokeStyle = "rgba(255, 255, 0, 0.5)";
    ctx.lineWidth = 2;
    ctx.beginPath();
    ctx.moveTo(bullet.x, bullet.y);
    ctx.lineTo(
      bullet.x - Math.cos(bullet.angle) * 10,
      bullet.y - Math.sin(bullet.angle) * 10
    );
    ctx.stroke();
  });

  ctx.restore();
}

gameLoop();