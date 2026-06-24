/* State */
let state = {
  token: localStorage.getItem('token') || null,
  user: null,
  activityTypes: [],
  currentTypeId: null,
  isLogin: true
};

/* API */
const api = {
  async request(method, path, body) {
    const headers = { 'Content-Type': 'application/json' };
    if (state.token) headers['Authorization'] = `Bearer ${state.token}`;
    const res = await fetch(path, { method, headers, body: body ? JSON.stringify(body) : undefined });
    if (!res.ok) {
      const err = await res.json().catch(() => ({ message: res.statusText }));
      throw new Error(err.message || 'Request failed');
    }
    return res.status === 204 ? null : res.json();
  },
  register: (data) => api.request('POST', '/api/v1/auth/register', data),
  login: (data) => api.request('POST', '/api/v1/auth/login', data),
  getProfile: () => api.request('GET', '/api/v1/users/me'),
  listTypes: (page) => api.request('GET', `/api/v1/activity-types?page=${page || 0}&size=20`),
  createType: (data) => api.request('POST', '/api/v1/activity-types', data),
  getType: (id) => api.request('GET', `/api/v1/activity-types/${id}`),
  listEntries: (typeId, page) => api.request('GET', `/api/v1/entries?activityTypeId=${typeId}&page=${page || 0}&size=50`),
  createEntry: (data) => api.request('POST', '/api/v1/entries', data),
  getStats: (typeId, from, to) => api.request('GET', `/api/v1/entries/stats?activityTypeId=${typeId}&from=${from}&to=${to}`)
};

/* Navigation */
function showPage(id) {
  document.querySelectorAll('.page').forEach(p => p.classList.add('hidden'));
  document.getElementById(`page-${id}`).classList.remove('hidden');
}

function toggleAuth() {
  state.isLogin = !state.isLogin;
  document.getElementById('auth-title').textContent = state.isLogin ? 'Sign In' : 'Sign Up';
  document.getElementById('auth-submit').textContent = state.isLogin ? 'Sign In' : 'Sign Up';
  document.getElementById('auth-name-group').classList.toggle('hidden', state.isLogin);
  document.getElementById('auth-toggle-text').textContent = state.isLogin ? "Don't have an account?" : 'Already have an account?';
  document.getElementById('auth-toggle-link').textContent = state.isLogin ? 'Sign Up' : 'Sign In';
  document.getElementById('auth-error').classList.add('hidden');
}

function showDashboard() { showPage('dashboard'); loadTypes(); }
function showCreateType() { showPage('create-type'); resetCreateTypeForm(); }
function showDetail() { showPage('detail'); loadDetail(); }
function showCreateEntry() { showPage('create-entry'); resetCreateEntryForm(); }

/* Auth */
async function handleAuth(e) {
  e.preventDefault();
  const email = document.getElementById('auth-email').value;
  const password = document.getElementById('auth-password').value;
  const name = document.getElementById('auth-name').value;
  const errEl = document.getElementById('auth-error');
  errEl.classList.add('hidden');

  try {
    const res = state.isLogin ? await api.login({ email, password }) : await api.register({ email, password, displayName: name });
    state.token = res.token;
    state.user = res.user;
    localStorage.setItem('token', res.token);
    showApp();
  } catch (err) {
    errEl.textContent = err.message;
    errEl.classList.remove('hidden');
  }
  return false;
}

function logout() {
  state.token = null;
  state.user = null;
  localStorage.removeItem('token');
  document.getElementById('header').classList.add('hidden');
  showPage('auth');
}

/* App */
async function showApp() {
  document.getElementById('header').classList.remove('hidden');
  try {
    state.user = await api.getProfile();
    document.getElementById('header-user').textContent = state.user.displayName;
  } catch (_) {}
  showDashboard();
}

async function loadTypes() {
  const container = document.getElementById('type-list');
  container.innerHTML = '<div class="empty">Loading...</div>';
  try {
    const res = await api.listTypes(0);
    if (res.content.length === 0) {
      container.innerHTML = '<div class="empty">No activity types yet. Create your first one!</div>';
      return;
    }
    container.innerHTML = res.content.map(t => `
      <div class="type-card" onclick="selectType(${t.id})">
        <div>
          <h3>${t.icon ? t.icon + ' ' : ''}${esc(t.name)}</h3>
          ${t.description ? `<p>${esc(t.description)}</p>` : ''}
        </div>
        <span class="badge">${t.fields.length} field${t.fields.length !== 1 ? 's' : ''}</span>
      </div>
    `).join('');
  } catch (err) {
    container.innerHTML = `<div class="empty">Error: ${err.message}</div>`;
  }
}

let currentFieldCount = 0;

function resetCreateTypeForm() {
  document.getElementById('create-type-form').reset();
  document.getElementById('fields-container').innerHTML = '';
  document.getElementById('create-type-error').classList.add('hidden');
  currentFieldCount = 0;
}

function addField() {
  const container = document.getElementById('fields-container');
  const i = currentFieldCount++;
  const div = document.createElement('div');
  div.className = 'field-row';
  div.innerHTML = `
    <input type="text" name="field-name-${i}" placeholder="Field name" required>
    <select name="field-type-${i}">
      <option value="NUMERIC">Number</option>
      <option value="TEXT">Text</option>
      <option value="BOOLEAN">Yes/No</option>
    </select>
    <button type="button" class="btn btn-sm btn-outline" onclick="this.parentElement.remove()">X</button>
  `;
  container.appendChild(div);
}

async function handleCreateType(e) {
  e.preventDefault();
  const errEl = document.getElementById('create-type-error');
  errEl.classList.add('hidden');

  const fields = [];
  const fieldRows = document.querySelectorAll('#fields-container .field-row');
  fieldRows.forEach((row, i) => {
    fields.push({
      fieldName: row.querySelector('input').value,
      fieldType: row.querySelector('select').value,
      required: false,
      displayOrder: i
    });
  });

  const body = {
    name: document.getElementById('type-name').value,
    description: document.getElementById('type-desc').value || undefined,
    icon: document.getElementById('type-icon').value || undefined,
    fields
  };

  try {
    await api.createType(body);
    showDashboard();
  } catch (err) {
    errEl.textContent = err.message;
    errEl.classList.remove('hidden');
  }
  return false;
}

/* Detail */
async function selectType(id) {
  state.currentTypeId = id;
  showDetail();
}

async function loadDetail() {
  if (!state.currentTypeId) return;
  try {
    const type = await api.getType(state.currentTypeId);
    document.getElementById('detail-title').textContent = `${type.icon ? type.icon + ' ' : ''}${esc(type.name)}`;

    const from = '2020-01-01';
    const to = new Date().toISOString().split('T')[0];
    const stats = await api.getStats(state.currentTypeId, from, to);

    const statsCard = document.getElementById('stats-card');
    let statsHtml = '<div class="card-title">Statistics</div><div class="stats-grid">';
    statsHtml += `<div class="stat-item"><div class="stat-value">${stats.totalEntries}</div><div class="stat-label">Entries</div></div>`;
    statsHtml += `<div class="stat-item"><div class="stat-value">${stats.currentStreak}</div><div class="stat-label">Current Streak</div></div>`;
    statsHtml += `<div class="stat-item"><div class="stat-value">${stats.longestStreak}</div><div class="stat-label">Best Streak</div></div>`;
    if (stats.averages) {
      Object.entries(stats.averages).forEach(([k, v]) => {
        statsHtml += `<div class="stat-item"><div class="stat-value">${Number(v).toFixed(1)}</div><div class="stat-label">Avg ${k.replace(/_/g, ' ')}</div></div>`;
      });
    }
    if (stats.totals) {
      Object.entries(stats.totals).forEach(([k, v]) => {
        statsHtml += `<div class="stat-item"><div class="stat-value">${Number(v).toFixed(1)}</div><div class="stat-label">Total ${k.replace(/_/g, ' ')}</div></div>`;
      });
    }
    statsHtml += '</div>';
    statsCard.innerHTML = statsHtml;

    const entriesList = document.getElementById('entries-list');
    const res = await api.listEntries(state.currentTypeId, 0);
    if (res.content.length === 0) {
      entriesList.innerHTML = '<div class="card-title">Entries</div><div class="empty">No entries yet.</div>';
      return;
    }
    let html = '<div class="card-title">Recent Entries</div>';
    res.content.forEach(e => {
      html += '<div class="entry-item">';
      html += `<span class="entry-date">${e.entryDate}</span>`;
      if (e.notes) html += `<span class="entry-notes"> &mdash; ${esc(e.notes)}</span>`;
      html += '<br>';
      e.fieldValues.forEach(fv => {
        html += `<span class="entry-field"><strong>${esc(fv.fieldName)}:</strong> ${esc(fv.value)}</span>`;
      });
      html += '</div>';
    });
    entriesList.innerHTML = html;
  } catch (err) {
    document.getElementById('stats-card').innerHTML = `<div class="empty">Error: ${err.message}</div>`;
  }
}

/* Create Entry */
async function resetCreateEntryForm() {
  document.getElementById('create-entry-form').reset();
  document.getElementById('entry-date').value = new Date().toISOString().split('T')[0];
  document.getElementById('create-entry-error').classList.add('hidden');
  const container = document.getElementById('entry-fields-container');
  container.innerHTML = '<div class="empty">Loading fields...</div>';

  try {
    const type = await api.getType(state.currentTypeId);
    container.innerHTML = '';
    type.fields.forEach(f => {
      const group = document.createElement('div');
      group.className = 'form-group';
      const label = document.createElement('label');
      label.textContent = f.fieldName.replace(/_/g, ' ');
      group.appendChild(label);

      if (f.fieldType === 'BOOLEAN') {
        const select = document.createElement('select');
        select.dataset.fieldDefId = f.id;
        select.innerHTML = '<option value="">Select...</option><option value="true">Yes</option><option value="false">No</option>';
        group.appendChild(select);
      } else if (f.fieldType === 'NUMERIC') {
        const input = document.createElement('input');
        input.type = 'number';
        input.step = 'any';
        input.dataset.fieldDefId = f.id;
        input.placeholder = f.fieldName.replace(/_/g, ' ');
        group.appendChild(input);
      } else {
        const input = document.createElement('input');
        input.type = 'text';
        input.dataset.fieldDefId = f.id;
        input.placeholder = f.fieldName.replace(/_/g, ' ');
        group.appendChild(input);
      }
      container.appendChild(group);
    });
  } catch (err) {
    container.innerHTML = `<div class="empty">Error: ${err.message}</div>`;
  }
}

async function handleCreateEntry(e) {
  e.preventDefault();
  const errEl = document.getElementById('create-entry-error');
  errEl.classList.add('hidden');

  const fieldValues = [];
  document.querySelectorAll('#entry-fields-container .form-group').forEach(group => {
    const input = group.querySelector('input, select');
    if (!input) return;
    const val = input.value;
    if (val) {
      fieldValues.push({ fieldDefId: parseInt(input.dataset.fieldDefId), value: val });
    }
  });

  const body = {
    activityTypeId: state.currentTypeId,
    entryDate: document.getElementById('entry-date').value,
    notes: document.getElementById('entry-notes').value || undefined,
    fieldValues
  };

  try {
    await api.createEntry(body);
    showDetail();
  } catch (err) {
    errEl.textContent = err.message;
    errEl.classList.remove('hidden');
  }
  return false;
}

/* Utils */
function esc(s) {
  const d = document.createElement('div');
  d.textContent = s;
  return d.innerHTML;
}

/* Init */
document.addEventListener('DOMContentLoaded', () => {
  if (state.token) {
    showApp();
  } else {
    showPage('auth');
  }
});
