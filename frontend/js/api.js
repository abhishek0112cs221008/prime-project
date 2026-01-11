// const API_BASE = 'https://YOUR-RENDER-SERVICE-NAME.onrender.com/api'; // TODO: Replace with your actual Render URL
const API_BASE = 'https://prime-project.onrender.com/api';
class Api {
    static async request(endpoint, method = 'GET', body = null) {
        // Read Token
        let token = null;
        let user = null;
        try {
            user = JSON.parse(localStorage.getItem('user'));
            token = user ? user.token : null;
        } catch (e) { }

        const headers = { 'Content-Type': 'application/json' };
        if (token) headers['X-Auth-Token'] = token;

        const config = {
            method,
            headers,
            credentials: 'include' // Still useful, but token is primary now
        };

        if (body) config.body = JSON.stringify(body);

        try {
            const response = await fetch(`${API_BASE}${endpoint}`, config);

            // Handle Session Expiry / Unauthorized
            if (response.status === 401) {
                if (localStorage.getItem('user')) {
                    alert("Your session has expired. Please sign in again.");
                    logout();
                }
                return null;
            }

            if (response.status === 403) {
                throw new Error("Access Denied: You do not have permission.");
            }

            // For DELETE calls or empty responses (204 No Content), don't parse JSON
            if (response.status === 204 || response.headers.get('content-length') === '0') {
                return null;
            }

            const data = await response.json();

            if (!response.ok) {
                // If data is just a string, throw it. If it has message, use it.
                // If it is validation errors (Spring Validation), format it.
                if (data.errors) { // common spring validation structure
                    throw new Error(Object.values(data.errors).join(', '));
                }
                throw new Error(data.message || JSON.stringify(data) || 'Something went wrong');
            }
            return data;
        } catch (error) {
            console.error(error);
            throw error;
        }
    }

    static get(endpoint) { return this.request(endpoint, 'GET'); }
    static post(endpoint, body) { return this.request(endpoint, 'POST', body); }
    static put(endpoint, body) { return this.request(endpoint, 'PUT', body); }
    static delete(endpoint) { return this.request(endpoint, 'DELETE'); }
}

// Auth Helpers
function checkLogin() {
    const userStr = localStorage.getItem('user');
    if (userStr) {
        const u = JSON.parse(userStr);
        // Backend returns wrapped, so we might need u.user or u if structure changed.
        // Update login logic to store { ...user, token: ... }
        return u.user || u;
    }
    return null;
}

function requireLogin() {
    const user = checkLogin();
    if (!user) {
        window.location.href = 'login.html';
        return false;
    }
    return true;
}

async function logout(event) {
    if (event) event.preventDefault();
    try {
        await Api.post('/auth/logout');
    } catch (e) {
        console.warn("Server logout failed:", e);
    } finally {
        localStorage.removeItem('user');
        window.location.href = 'index.html';
    }
}

// UI Helpers
function updateNavbar() {
    const user = checkLogin();
    const authLinks = document.getElementById('auth-links');
    if (!authLinks) return;

    if (user) {
        authLinks.innerHTML = '<a href="#" onclick="logout(event)">Sign Out</a>';
        const dashLink = document.getElementById('dash-link');
        const adminPanel = document.getElementById('admin-panel');

        if (user.role === 'admin') {
            if (dashLink) dashLink.style.display = 'inline-block';
            if (adminPanel) adminPanel.style.display = 'block';
        }
    } else {
        authLinks.innerHTML = '<a href="login.html" class="nav-btn">Sign In</a>';
    }
}

// Theme Logic
function initTheme() {
    const savedTheme = localStorage.getItem('theme');
    const systemDark = window.matchMedia('(prefers-color-scheme: dark)').matches;

    if (savedTheme) {
        document.documentElement.setAttribute('data-theme', savedTheme);
    } else if (systemDark) {
        document.documentElement.setAttribute('data-theme', 'dark');
    }
}

function toggleTheme() {
    const current = document.documentElement.getAttribute('data-theme');
    const newTheme = current === 'dark' ? 'light' : 'dark';
    document.documentElement.setAttribute('data-theme', newTheme);
    localStorage.setItem('theme', newTheme);
}

initTheme();
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', updateNavbar);
} else {
    updateNavbar();
}
