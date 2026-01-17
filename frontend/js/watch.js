let previousTime = {};

function updateFlipUnit(id, value) {
    const el = document.getElementById(id);
    if (!el) return;

    if (previousTime[id] !== value) {
        el.textContent = value;
        previousTime[id] = value;
    }
}

function updateWatch() {
    const now = new Date();
    let hours = now.getHours();
    const ampm = hours >= 12 ? 'PM' : 'AM';
    hours = hours % 12;
    hours = hours ? hours : 12;

    const h = hours.toString().padStart(2, '0');
    const m = now.getMinutes().toString().padStart(2, '0');
    const s = now.getSeconds().toString().padStart(2, '0');

    updateFlipUnit('hr1', h[0]);
    updateFlipUnit('hr2', h[1]);
    updateFlipUnit('min1', m[0]);
    updateFlipUnit('min2', m[1]);
    updateFlipUnit('sec1', s[0]);
    updateFlipUnit('sec2', s[1]);

    const ampmEl = document.getElementById('ampm');
    if (ampmEl && previousTime.ampm !== ampm) {
        ampmEl.textContent = ampm;
        previousTime.ampm = ampm;
    }
}

// Ensure the units exist before initial call
document.addEventListener('DOMContentLoaded', () => {
    updateWatch();
    setInterval(updateWatch, 1000);
});
