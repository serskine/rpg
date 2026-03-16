// Global state
let worldData = null;
let roomPositions = new Map();
let selectedRoom = null;
let canvasState = {
    scale: 50,
    offsetX: 0,
    offsetY: 0,
    lastMouseX: 0,
    lastMouseY: 0
};

// Initialize app
document.addEventListener('DOMContentLoaded', () => {
    setupEventListeners();
    loadWorld();
});

function setupEventListeners() {
    // Tab switching
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.addEventListener('click', () => switchTab(btn.dataset.tab));
    });

    // Generate button
    document.getElementById('generateBtn').addEventListener('click', generateWorld);

    // Canvas interactions
    const canvas = document.getElementById('dungeonCanvas');
    canvas.addEventListener('mousedown', onCanvasMouseDown);
    canvas.addEventListener('mousemove', onCanvasMouseMove);
    canvas.addEventListener('mouseup', onCanvasMouseUp);
    canvas.addEventListener('wheel', onCanvasWheel);
}

function switchTab(tabName) {
    // Hide all tabs
    document.querySelectorAll('.tab-content').forEach(tab => tab.classList.remove('active'));
    document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));

    // Show selected tab
    document.getElementById(tabName).classList.add('active');
    event.target.classList.add('active');

    // Redraw canvas if switching to dungeon map
    if (tabName === 'dungeonMap' && worldData) {
        setTimeout(() => redrawCanvas(), 100);
    }
}

async function loadWorld() {
    try {
        const response = await fetch('/api/world');
        worldData = await response.json();
        updateStats();
        drawWorld();
        buildTreeView();
        centerCanvasView();
    } catch (error) {
        console.error('Error loading world:', error);
    }
}

async function generateWorld() {
    const partySize = parseInt(document.getElementById('partySizeInput').value);
    const partyLevel = parseInt(document.getElementById('partyLevelInput').value);

    try {
        const response = await fetch(`/api/world/generate?partySize=${partySize}&partyLevel=${partyLevel}`);
        worldData = await response.json();
        updateStats();
        roomPositions.clear();
        selectedRoom = null;
        drawWorld();
        buildTreeView();
        centerCanvasView();
    } catch (error) {
        console.error('Error generating world:', error);
    }
}

function updateStats() {
    if (!worldData) return;
    document.getElementById('statRooms').textContent = worldData.stats.totalRooms;
    document.getElementById('statPaths').textContent = worldData.stats.totalPaths;
    document.getElementById('statPartySize').textContent = worldData.stats.partySize;
    document.getElementById('statPartyLevel').textContent = worldData.stats.partyLevel;
}

// ===== DUNGEON MAP CANVAS =====

function drawWorld() {
    initializeLayout();
    redrawCanvas();
}

function initializeLayout() {
    if (!worldData || worldData.rooms.length === 0) return;

    // Initialize room positions with grid-based force layout
    if (roomPositions.size === 0) {
        const random = new SeededRandom(42);
        for (let i = 0; i < worldData.rooms.length; i++) {
            roomPositions.set(i, {
                x: random.nextInt(40),
                y: random.nextInt(40)
            });
        }

        // Run force-directed layout
        runGridForceLayout();
    }
}

function runGridForceLayout() {
    const iterations = 2000;
    const k = 5.0;

    for (let iter = 0; iter < iterations; iter++) {
        const forces = new Map();
        for (let i = 0; i < worldData.rooms.length; i++) {
            forces.set(i, { x: 0, y: 0 });
        }

        // Repulsive forces
        for (let i = 0; i < worldData.rooms.length; i++) {
            for (let j = 0; j < worldData.rooms.length; j++) {
                if (i === j) continue;

                const pi = roomPositions.get(i);
                const pj = roomPositions.get(j);
                const dx = pi.x - pj.x;
                const dy = pi.y - pj.y;
                const distSq = dx * dx + dy * dy;
                const dist = Math.sqrt(distSq);

                if (dist > 0) {
                    const minSep = 8.0;
                    let force = (k * k) / dist;
                    if (dist < minSep) force *= 5.0;

                    forces.get(i).x += (dx / dist) * force;
                    forces.get(i).y += (dy / dist) * force;
                }
            }
        }

        // Attractive forces (springs)
        for (const path of (worldData.paths || [])) {
            const pi = roomPositions.get(path.from);
            const pj = roomPositions.get(path.to);
            const dx = pi.x - pj.x;
            const dy = pi.y - pj.y;
            const dist = Math.sqrt(dx * dx + dy * dy);

            const targetDist = (path.distance === 'MELEE' ? 0 : path.distance === 'SHORT' ? 1 : 2) + 3;

            if (dist > 0) {
                const displacement = dist - targetDist;
                const force = displacement * 0.5;

                forces.get(path.from).x -= (dx / dist) * force;
                forces.get(path.from).y -= (dy / dist) * force;
                forces.get(path.to).x += (dx / dist) * force;
                forces.get(path.to).y += (dy / dist) * force;
            }
        }

        // Apply forces
        const temp = 10.0 * Math.exp(-iter / iterations * 5);

        for (let i = 0; i < worldData.rooms.length; i++) {
            const f = forces.get(i);
            const fMag = Math.sqrt(f.x * f.x + f.y * f.y);

            if (fMag > 0) {
                const p = roomPositions.get(i);
                const moveX = (f.x / fMag) * Math.min(fMag, temp);
                const moveY = (f.y / fMag) * Math.min(fMag, temp);

                p.x += Math.round(moveX);
                p.y += Math.round(moveY);
            }
        }
    }

    // Resolve collisions
    resolveCollisions();
}

function resolveCollisions() {
    let changed = true;
    let iter = 0;

    while (changed && iter < 100) {
        changed = false;
        for (let i = 0; i < worldData.rooms.length; i++) {
            for (let j = i + 1; j < worldData.rooms.length; j++) {
                const p1 = roomPositions.get(i);
                const p2 = roomPositions.get(j);

                const size1 = 2; // getRoomSize(worldData.rooms[i])
                const size2 = 2;

                const rect1 = {
                    x: p1.x - size1 / 2,
                    y: p1.y - size1 / 2,
                    width: size1,
                    height: size1
                };
                const rect2 = {
                    x: p2.x - size2 / 2,
                    y: p2.y - size2 / 2,
                    width: size2,
                    height: size2
                };

                if (rectsIntersect(rect1, rect2)) {
                    if (p1.x < p2.x) p1.x--; else p1.x++;
                    if (p1.y < p2.y) p1.y--; else p1.y++;
                    changed = true;
                }
            }
        }
        iter++;
    }
}

function rectsIntersect(r1, r2) {
    return r1.x < r2.x + r2.width &&
           r1.x + r1.width > r2.x &&
           r1.y < r2.y + r2.height &&
           r1.y + r1.height > r2.y;
}

function redrawCanvas() {
    const canvas = document.getElementById('dungeonCanvas');
    const ctx = canvas.getContext('2d');

    // Clear canvas
    ctx.fillStyle = '#2d2d2d';
    ctx.fillRect(0, 0, canvas.width, canvas.height);

    if (!worldData) return;

    // Save context
    ctx.save();

    // Apply transforms
    ctx.translate(canvasState.offsetX, canvasState.offsetY);
    ctx.scale(canvasState.scale, canvasState.scale);

    // Draw paths
    for (const path of (worldData.paths || [])) {
        drawPath(ctx, path);
    }

    // Draw rooms
    for (let i = 0; i < worldData.rooms.length; i++) {
        drawRoom(ctx, i);
    }

    // Draw creatures
    for (let i = 0; i < worldData.rooms.length; i++) {
        const room = worldData.rooms[i];
        if (room.creatures && room.creatures.length > 0) {
            drawCreatures(ctx, i, room);
        }
    }

    // Restore context
    ctx.restore();
}

function drawPath(ctx, path) {
    const p1 = roomPositions.get(path.from);
    const p2 = roomPositions.get(path.to);

    if (!p1 || !p2) return;

    // Simple line between room centers
    ctx.strokeStyle = '#444444';
    ctx.lineWidth = 0.1;
    ctx.beginPath();
    ctx.moveTo(p1.x, p1.y);
    ctx.lineTo(p2.x, p2.y);
    ctx.stroke();

    // Draw distance label at midpoint
    const midX = (p1.x + p2.x) / 2;
    const midY = (p1.y + p2.y) / 2;
    ctx.save();
    ctx.scale(1 / canvasState.scale, 1 / canvasState.scale);
    ctx.translate(midX * canvasState.scale, midY * canvasState.scale);
    ctx.fillStyle = '#000000';
    ctx.font = '12px Arial';
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';
    ctx.fillText(path.distance, 0, 0);
    ctx.restore();
}

function drawRoom(ctx, roomId) {
    const room = worldData.rooms[roomId];
    const pos = roomPositions.get(roomId);

    if (!pos) return;

    const size = 2; // TODO: base on room.size
    const rect = {
        x: pos.x - size / 2,
        y: pos.y - size / 2,
        width: size,
        height: size
    };

    // Draw room rectangle
    ctx.fillStyle = selectedRoom === roomId ? '#5555ff' : '#333333';
    ctx.fillRect(rect.x, rect.y, rect.width, rect.height);
    ctx.strokeStyle = '#666666';
    ctx.lineWidth = 0.05;
    ctx.strokeRect(rect.x, rect.y, rect.width, rect.height);

    // Draw title
    ctx.save();
    ctx.scale(1 / canvasState.scale, 1 / canvasState.scale);
    ctx.translate(pos.x * canvasState.scale, (rect.y - 0.3) * canvasState.scale);
    ctx.fillStyle = '#0000ff';
    ctx.font = 'bold 14px Arial';
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';
    ctx.fillText(room.title, 0, 0);
    ctx.restore();
}

function drawCreatures(ctx, roomId, room) {
    const pos = roomPositions.get(roomId);
    if (!pos) return;

    const size = 2;
    const rect = {
        x: pos.x - size / 2,
        y: pos.y - size / 2,
        width: size,
        height: size
    };

    let col = 0, row = 0;
    for (const creature of room.creatures) {
        const creatureSize = 0.5;
        let x = rect.x + 0.2 + col * 0.6;
        let y = rect.y + 0.2 + row * 0.6;

        if (x + creatureSize > rect.x + rect.width - 0.1) {
            col = 0;
            row++;
            x = rect.x + 0.2;
            y = rect.y + 0.2 + row * 0.6;
        }

        const color = getJobColor(creature.job);
        ctx.fillStyle = color;
        ctx.fillRect(x, y, creatureSize, creatureSize);
        ctx.strokeStyle = '#000000';
        ctx.lineWidth = 0.03;
        ctx.strokeRect(x, y, creatureSize, creatureSize);

        col++;
        if (col > 2) {
            col = 0;
            row++;
        }
    }
}

function getJobColor(job) {
    const colors = {
        'FIGHTER': '#8b4513',
        'WIZARD': '#4b0082',
        'ROGUE': '#228b22',
        'CLERIC': '#ffd700',
        'PALADIN': '#dc143c',
        'RANGER': '#006400',
        'BARBARIAN': '#b22222',
        'MONK': '#808000',
        'BARD': '#9400d3',
        'DRUID': '#008000',
        'WARLOCK': '#4b0082',
        'SORCERER': '#ff0000',
        'COMMONER': '#808080'
    };
    return colors[job] || '#808080';
}

function centerCanvasView() {
    if (roomPositions.size === 0) return;

    let minX = Infinity, maxX = -Infinity;
    let minY = Infinity, maxY = -Infinity;

    for (const pos of roomPositions.values()) {
        minX = Math.min(minX, pos.x);
        maxX = Math.max(maxX, pos.x);
        minY = Math.min(minY, pos.y);
        maxY = Math.max(maxY, pos.y);
    }

    const centerX = (minX + maxX) / 2;
    const centerY = (minY + maxY) / 2;

    const canvas = document.getElementById('dungeonCanvas');
    canvasState.offsetX = canvas.width / 2 - centerX * canvasState.scale;
    canvasState.offsetY = canvas.height / 2 - centerY * canvasState.scale;

    redrawCanvas();
}

// Canvas event handlers
function onCanvasMouseDown(e) {
    const canvas = document.getElementById('dungeonCanvas');
    const rect = canvas.getBoundingClientRect();
    const screenX = e.clientX - rect.left;
    const screenY = e.clientY - rect.top;

    // Check if room was clicked
    const worldX = (screenX - canvasState.offsetX) / canvasState.scale;
    const worldY = (screenY - canvasState.offsetY) / canvasState.scale;

    for (let i = 0; i < worldData.rooms.length; i++) {
        const pos = roomPositions.get(i);
        if (pos && worldX >= pos.x - 1 && worldX <= pos.x + 1 &&
            worldY >= pos.y - 1 && worldY <= pos.y + 1) {
            selectRoom(i);
            return;
        }
    }

    canvasState.lastMouseX = screenX;
    canvasState.lastMouseY = screenY;
}

function onCanvasMouseMove(e) {
    if (canvasState.lastMouseX === undefined) return;

    const canvas = document.getElementById('dungeonCanvas');
    const rect = canvas.getBoundingClientRect();
    const screenX = e.clientX - rect.left;
    const screenY = e.clientY - rect.top;

    if (e.buttons === 1) {
        const dx = screenX - canvasState.lastMouseX;
        const dy = screenY - canvasState.lastMouseY;

        canvasState.offsetX += dx;
        canvasState.offsetY += dy;

        canvasState.lastMouseX = screenX;
        canvasState.lastMouseY = screenY;

        redrawCanvas();
    }
}

function onCanvasMouseUp(e) {
    canvasState.lastMouseX = undefined;
    canvasState.lastMouseY = undefined;
}

function onCanvasWheel(e) {
    e.preventDefault();

    const canvas = document.getElementById('dungeonCanvas');
    const rect = canvas.getBoundingClientRect();
    const screenX = e.clientX - rect.left;
    const screenY = e.clientY - rect.top;

    const zoomFactor = 1.1;
    const oldScale = canvasState.scale;

    if (e.deltaY < 0) {
        canvasState.scale *= zoomFactor;
    } else {
        canvasState.scale /= zoomFactor;
    }

    // Clamp zoom
    canvasState.scale = Math.max(1, Math.min(500, canvasState.scale));

    // Adjust offset to zoom toward mouse pointer
    const worldX = (screenX - canvasState.offsetX) / oldScale;
    const worldY = (screenY - canvasState.offsetY) / oldScale;

    canvasState.offsetX = screenX - worldX * canvasState.scale;
    canvasState.offsetY = screenY - worldY * canvasState.scale;

    redrawCanvas();
}

function selectRoom(roomId) {
    selectedRoom = roomId;
    showRoomPreview(roomId);
    redrawCanvas();
}

// ===== TREE VIEW =====

function buildTreeView() {
    const treeView = document.getElementById('treeView');
    treeView.innerHTML = '';

    if (!worldData) return;

    // Dungeon section
    const dungeonSection = document.createElement('div');
    const dungeonTitle = document.createElement('div');
    dungeonTitle.className = 'tree-node parent';
    dungeonTitle.textContent = '▼ Dungeon';
    dungeonTitle.addEventListener('click', () => toggleTreeSection(dungeonSection));
    dungeonSection.appendChild(dungeonTitle);

    for (let i = 0; i < worldData.rooms.length; i++) {
        const room = worldData.rooms[i];
        const roomNode = document.createElement('div');
        roomNode.className = 'tree-node child';
        roomNode.textContent = room.title;
        roomNode.addEventListener('click', () => selectRoom(i));
        
        // Create expandable room details
        const detailsDiv = document.createElement('div');
        detailsDiv.className = 'tree-node child';
        detailsDiv.style.marginLeft = '30px';
        detailsDiv.style.fontSize = '11px';
        detailsDiv.style.color = '#999';

        let details = `Type: ${room.type} | Size: ${room.size}`;
        if (room.creatures.length > 0) {
            details += ` | Creatures: ${room.creatures.length}`;
        }
        if (room.features.length > 0) {
            details += ` | Features: ${room.features.length}`;
        }
        detailsDiv.textContent = details;
        detailsDiv.style.display = 'none';

        roomNode.addEventListener('click', (e) => {
            e.stopPropagation();
            selectRoom(i);
            detailsDiv.style.display = detailsDiv.style.display === 'none' ? 'block' : 'none';
        });

        dungeonSection.appendChild(roomNode);
        dungeonSection.appendChild(detailsDiv);
    }

    treeView.appendChild(dungeonSection);

    // Parties section
    const partiesSection = document.createElement('div');
    const partiesTitle = document.createElement('div');
    partiesTitle.className = 'tree-node parent';
    partiesTitle.textContent = '▼ Parties';
    partiesTitle.addEventListener('click', () => toggleTreeSection(partiesSection));
    partiesSection.appendChild(partiesTitle);

    for (const party of (worldData.parties || [])) {
        const partyNode = document.createElement('div');
        partyNode.className = 'tree-node child';
        partyNode.textContent = party.title;
        partyNode.style.marginLeft = '15px';

        const creaturesDiv = document.createElement('div');
        creaturesDiv.style.marginLeft = '30px';
        creaturesDiv.style.display = 'none';

        for (const creature of party.creatures) {
            const creatureNode = document.createElement('div');
            creatureNode.className = 'tree-node child';
            creatureNode.style.fontSize = '11px';
            creatureNode.textContent = `${creature.name} (${creature.job} Lvl ${creature.level})`;
            creatureNode.addEventListener('click', () => showCreaturePreview(creature));
            creaturesDiv.appendChild(creatureNode);
        }

        partyNode.addEventListener('click', (e) => {
            e.stopPropagation();
            creaturesDiv.style.display = creaturesDiv.style.display === 'none' ? 'block' : 'none';
        });

        partiesSection.appendChild(partyNode);
        partiesSection.appendChild(creaturesDiv);
    }

    treeView.appendChild(partiesSection);
}

function toggleTreeSection(section) {
    // Simple toggle visibility
}

// ===== PREVIEW PANELS =====

function showRoomPreview(roomId) {
    const room = worldData.rooms[roomId];
    const previewContent = document.getElementById('previewContent');

    let html = `
        <div class="preview-section">
            <h3>${room.title}</h3>
            <div class="preview-item"><strong>Type:</strong> ${room.type}</div>
            <div class="preview-item"><strong>Size:</strong> ${room.size} (${room.numSquares} sq ft)</div>
        </div>
    `;

    if (room.features && room.features.length > 0) {
        html += `<div class="preview-section">
            <h3>Features (${room.features.length})</h3>`;
        for (const feature of room.features) {
            html += `<div class="preview-item">• ${feature.name}</div>`;
        }
        html += `</div>`;
    }

    if (room.creatures && room.creatures.length > 0) {
        html += `<div class="preview-section">
            <h3>Creatures (${room.creatures.length})</h3>`;
        for (const creature of room.creatures) {
            html += `<div class="preview-item">
                <strong>${creature.name}</strong><br>
                ${creature.job} Level ${creature.level} | HP: ${creature.hp}/${creature.maxHp}
            </div>`;
        }
        html += `</div>`;
    }

    // Connections
    if (worldData.paths) {
        const connections = worldData.paths.filter(p => p.from === roomId || p.to === roomId);
        if (connections.length > 0) {
            html += `<div class="preview-section">
                <h3>Connections (${connections.length})</h3>`;
            for (const path of connections) {
                const targetId = path.from === roomId ? path.to : path.from;
                const targetRoom = worldData.rooms[targetId];
                html += `<div class="preview-item">
                    To: <strong>${targetRoom.title}</strong> (${path.distance})
                    ${path.stealthDc ? `<br>Stealth DC: ${path.stealthDc}` : ''}
                    ${path.lockDc ? `<br>Lock DC: ${path.lockDc}` : ''}
                </div>`;
            }
            html += `</div>`;
        }
    }

    previewContent.innerHTML = html;
}

function showCreaturePreview(creature) {
    const previewContent = document.getElementById('previewContent');

    const html = `
        <div class="preview-section">
            <h3>${creature.name}</h3>
            <div class="preview-item"><strong>Job:</strong> ${creature.job}</div>
            <div class="preview-item"><strong>Level:</strong> ${creature.level}</div>
            <div class="preview-item"><strong>Alignment:</strong> ${creature.alignment}</div>
            <div class="preview-item"><strong>Size:</strong> ${creature.size}</div>
            <div class="preview-item"><strong>HP:</strong> ${creature.hp} / ${creature.maxHp}</div>
        </div>
        
        <div class="preview-section">
            <h3>Ability Scores</h3>
            <div class="stat-grid">
                <div class="stat-item">
                    <div class="stat-label">STR</div>
                    <div class="stat-value">${creature.strength}</div>
                </div>
                <div class="stat-item">
                    <div class="stat-label">DEX</div>
                    <div class="stat-value">${creature.dexterity}</div>
                </div>
                <div class="stat-item">
                    <div class="stat-label">CON</div>
                    <div class="stat-value">${creature.constitution}</div>
                </div>
                <div class="stat-item">
                    <div class="stat-label">INT</div>
                    <div class="stat-value">${creature.intelligence}</div>
                </div>
                <div class="stat-item">
                    <div class="stat-label">WIS</div>
                    <div class="stat-value">${creature.wisdom}</div>
                </div>
                <div class="stat-item">
                    <div class="stat-label">CHA</div>
                    <div class="stat-value">${creature.charisma}</div>
                </div>
            </div>
        </div>
    `;

    previewContent.innerHTML = html;
}

// ===== UTILITIES =====

class SeededRandom {
    constructor(seed) {
        this.seed = seed;
    }

    nextInt(max) {
        this.seed = (this.seed * 9301 + 49297) % 233280;
        return Math.floor((this.seed / 233280) * max);
    }
}
