// =============================================
// app.js — Frontend for PageTurner Java Backend
// Calls Java REST API at /api/*
// =============================================

const API = '/api';
const SHIPPING = 350;
const CAT_LABELS = {
  children: "Children's Books",
  novels:   "Novels",
  fiction:  "Fiction",
  romantic: "Romantic"
};

let currentUser    = null;
let selectedRating = 0;

// ── API Helper ────────────────────────────────────────────────────────────────
// All API calls go through here. Shows clear error messages if backend is down.
async function api(method, path, body) {
  const opts = {
    method,
    headers: { 'Content-Type': 'application/json' }
  };
  if (body) opts.body = JSON.stringify(body);

  let res;
  try {
    res = await fetch(API + path, opts);
  } catch (networkErr) {
    // Server not running at all
    throw new Error('Cannot reach server. Is the Java server running on port 8080?');
  }

  let data;
  try {
    data = await res.json();
  } catch {
    data = {};
  }

  if (!res.ok) {
    throw new Error(data.error || 'Server error ' + res.status);
  }
  return data;
}

// ── AUTH ──────────────────────────────────────────────────────────────────────
const Auth = {
  async login() {
    const username = document.getElementById('login-user').value.trim();
    const password = document.getElementById('login-pass').value;
    const msg      = document.getElementById('login-msg');
    hideMsg(msg);

    if (!username || !password) {
      showMsg(msg, 'error', 'Please fill in all fields.'); return;
    }
    try {
      const data = await api('POST', '/auth/login', { username, password });
      currentUser = { username: data.username, name: data.name, role: data.role };

      if (data.role === 'Admin') {
        showPage('page-admin');
        Admin.init();
      } else {
        document.getElementById('user-greeting').textContent =
          'Hi, ' + data.name.split(' ')[0];
        showPage('page-user');
        Cart.refreshBadge();
      }
    } catch (e) {
      showMsg(msg, 'error', e.message);
    }
  },

  async register() {
    const name     = document.getElementById('reg-name').value.trim();
    const username = document.getElementById('reg-user').value.trim();
    const email    = document.getElementById('reg-email').value.trim();
    const password = document.getElementById('reg-pass').value;
    const msg      = document.getElementById('reg-msg');
    hideMsg(msg);

    if (!name || !username || !email || !password) {
      showMsg(msg, 'error', 'All fields are required.'); return;
    }
    if (password.length < 6) {
      showMsg(msg, 'error', 'Password must be at least 6 characters.'); return;
    }
    try {
      await api('POST', '/auth/register', { name, username, email, password });
      showMsg(msg, 'success', 'Account created! You can now sign in.');
      setTimeout(() => showPage('page-login'), 1300);
    } catch (e) {
      showMsg(msg, 'error', e.message);
    }
  },

  logout() {
    currentUser = null;
    document.getElementById('login-user').value = '';
    document.getElementById('login-pass').value = '';
    hideMsg(document.getElementById('login-msg'));
    showPage('page-login');
  }
};

// ── BOOKS ─────────────────────────────────────────────────────────────────────
async function loadBooks(cat) {
  // Show section first with a loading indicator
  document.getElementById('books-title').textContent = CAT_LABELS[cat] || cat;
  document.getElementById('books-sub').textContent   =
    'Browse our ' + (CAT_LABELS[cat] || cat) + ' collection';
  document.getElementById('books-grid').innerHTML    =
    '<div style="padding:60px;text-align:center;color:var(--muted);font-size:16px">Loading books...</div>';
  showSection('books');

  try {
    const books = await api('GET', '/books?cat=' + cat);

    if (!books || books.length === 0) {
      document.getElementById('books-grid').innerHTML =
        '<div style="padding:60px;text-align:center;color:var(--muted)">' +
        '<div style="font-size:48px">📚</div>' +
        '<p style="margin-top:12px">No books found in this category.</p></div>';
      return;
    }

    document.getElementById('books-grid').innerHTML = books.map(b => `
      <div class="book-card">
        <div class="book-cover" onclick="openModal('${b.id}')">
          <img src="${b.img}" alt="${b.title}"
               onerror="this.parentElement.innerHTML='📚'">
        </div>
        <div class="book-info">
          <div class="book-title">${b.title}</div>
          <div class="book-author">${b.author}</div>
          <div class="book-price">LKR ${Number(b.price).toLocaleString()}</div>
          <div class="book-actions">
            <button class="btn btn-secondary btn-sm" onclick="openModal('${b.id}')">Details</button>
            <button class="btn btn-primary btn-sm"   onclick="Cart.add('${b.id}')">Add to Cart</button>
          </div>
        </div>
      </div>`).join('');

  } catch (e) {
    document.getElementById('books-grid').innerHTML =
      '<div style="padding:60px;text-align:center;color:#C0392B">' +
      '<div style="font-size:48px">⚠️</div>' +
      '<p style="margin-top:12px;font-weight:600">Failed to load books</p>' +
      '<p style="margin-top:8px;font-size:13px">' + e.message + '</p></div>';
    showToast('Error: ' + e.message);
  }
}

async function openModal(id) {
  document.getElementById('modal').classList.remove('hidden');
  document.getElementById('modal-body').innerHTML =
    '<div style="padding:40px;text-align:center;color:var(--muted)">Loading...</div>';

  try {
    const b = await api('GET', '/books/' + id);
    const stockCls = b.stock > 20 ? 'stock-ok' : b.stock > 0 ? 'stock-low' : 'stock-out';
    const stockLbl = b.stock > 20
      ? b.stock + ' available'
      : b.stock > 0
        ? 'Low — ' + b.stock + ' left'
        : 'Out of Stock';

    document.getElementById('modal-body').innerHTML = `
      <div class="modal-cover">
        <img src="${b.img}" alt="${b.title}"
             onerror="this.parentElement.innerHTML='📚'">
      </div>
      <h2>${b.title}</h2>
      <p class="author">by ${b.author}</p>
      <div class="price">LKR ${Number(b.price).toLocaleString()}</div>
      <div class="modal-meta">
        <div class="meta-item"><strong>Category:</strong> ${CAT_LABELS[b.cat] || b.cat}</div>
        <div class="meta-item"><strong>Pages:</strong> ${b.pages}</div>
        <div class="meta-item">
          <strong>Stock:</strong>
          <span class="stock-badge ${stockCls}">${stockLbl}</span>
        </div>
      </div>
      <p class="desc">${b.desc}</p>
      <button class="btn btn-primary" style="width:100%"
              onclick="Cart.add('${b.id}');closeModal()">
        Add to Cart 🛒
      </button>`;
  } catch (e) {
    document.getElementById('modal-body').innerHTML =
      '<div style="padding:40px;text-align:center;color:#C0392B">Failed to load book: ' +
      e.message + '</div>';
  }
}

function closeModal() {
  document.getElementById('modal').classList.add('hidden');
}

// ── CART ──────────────────────────────────────────────────────────────────────
const Cart = {
  async add(bookId) {
    try {
      await api('POST', '/cart', {
        username: currentUser.username,
        bookId,
        quantity: 1
      });
      this.refreshBadge();
      showToast('Added to cart! 🛒');
    } catch (e) {
      showToast('Error: ' + e.message);
    }
  },

  async remove(bookId) {
    try {
      await api('DELETE',
        '/cart?user=' + currentUser.username + '&bookId=' + bookId);
      await this.render();
      this.refreshBadge();
    } catch (e) {
      showToast('Error: ' + e.message);
    }
  },

  async updateQty(bookId, qty) {
    try {
      if (qty < 1) {
        await this.remove(bookId);
        return;
      }
      await api('PUT', '/cart', {
        username: currentUser.username,
        bookId,
        quantity: qty
      });
      await this.render();
    } catch (e) {
      showToast('Error: ' + e.message);
    }
  },

  async checkout() {
    try {
      const res = await api('POST', '/orders', {
        username: currentUser.username
      });
      this.refreshBadge();
      await this.render();
      showToast('Order placed! 🎉 Order #' + res.orderId);
    } catch (e) {
      showToast(e.message);
    }
  },

  async refreshBadge() {
    try {
      const items = await api('GET', '/cart?user=' + currentUser.username);
      const count = Array.isArray(items)
        ? items.reduce((s, i) => s + (i.quantity || 0), 0)
        : 0;
      document.getElementById('cart-badge').textContent = count;
    } catch {
      document.getElementById('cart-badge').textContent = '0';
    }
  },

  async render() {
    const el = document.getElementById('cart-content');
    el.innerHTML =
      '<div style="padding:40px;text-align:center;color:var(--muted)">Loading cart...</div>';

    try {
      const items = await api('GET', '/cart?user=' + currentUser.username);

      if (!items || items.length === 0) {
        el.innerHTML = `
          <div class="empty-cart">
            <div class="icon">🛒</div>
            <h3>Your cart is empty</h3>
            <p style="margin-top:10px;color:var(--muted)">
              Browse our books and add some to your cart!
            </p>
            <br>
            <button class="btn btn-primary" onclick="showSection('home')">
              Browse Books
            </button>
          </div>`;
        return;
      }

      let subtotal = 0;
      const rowPromises = items.map(async item => {
        try {
          const b = await api('GET', '/books/' + item.bookId);
          const lineTotal = b.price * item.quantity;
          subtotal += lineTotal;
          return `
            <tr>
              <td>
                <div style="display:flex;align-items:center;gap:12px">
                  <div style="width:50px;height:64px;border-radius:4px;overflow:hidden;flex-shrink:0">
                    <img src="${b.img}" style="width:100%;height:100%;object-fit:cover"
                         onerror="this.style.display='none'">
                  </div>
                  <div>
                    <div style="font-weight:600;font-size:14px">${b.title}</div>
                    <div style="color:var(--muted);font-size:12px">${b.author}</div>
                  </div>
                </div>
              </td>
              <td>LKR ${Number(b.price).toLocaleString()}</td>
              <td>
                <div class="qty-ctrl">
                  <button class="qty-btn"
                    onclick="Cart.updateQty('${b.id}', ${item.quantity - 1})">−</button>
                  <span style="font-weight:600;min-width:24px;text-align:center">
                    ${item.quantity}
                  </span>
                  <button class="qty-btn"
                    onclick="Cart.updateQty('${b.id}', ${item.quantity + 1})">+</button>
                </div>
              </td>
              <td style="font-weight:600">LKR ${lineTotal.toLocaleString()}</td>
              <td>
                <button class="btn btn-danger btn-sm"
                        onclick="Cart.remove('${b.id}')">Remove</button>
              </td>
            </tr>`;
        } catch {
          return ''; // skip book if fetch fails
        }
      });

      const rows = await Promise.all(rowPromises);
      const total = subtotal + SHIPPING;

      el.innerHTML = `
        <table class="cart-table">
          <thead>
            <tr>
              <th>Book</th><th>Price</th>
              <th>Quantity</th><th>Subtotal</th><th></th>
            </tr>
          </thead>
          <tbody>${rows.join('')}</tbody>
        </table>
        <div class="cart-sum">
          <div class="sum-row">
            <span>Subtotal</span>
            <span>LKR ${subtotal.toLocaleString()}</span>
          </div>
          <div class="sum-row">
            <span>Shipping</span>
            <span>LKR ${SHIPPING.toLocaleString()}</span>
          </div>
          <div class="sum-row">
            <span>Total</span>
            <span>LKR ${total.toLocaleString()}</span>
          </div>
          <div style="margin-top:20px;display:flex;gap:12px">
            <button class="btn btn-primary" onclick="Cart.checkout()">
              Proceed to Checkout
            </button>
            <button class="btn btn-secondary" onclick="showSection('home')">
              Continue Shopping
            </button>
          </div>
        </div>`;

    } catch (e) {
      el.innerHTML =
        '<div style="padding:40px;text-align:center;color:#C0392B">' +
        'Failed to load cart: ' + e.message + '</div>';
    }
  }
};

// ── FEEDBACK ──────────────────────────────────────────────────────────────────
const Feedback = {
  async submit() {
    const category  = document.getElementById('fb-cat').value;
    const bookTitle = document.getElementById('fb-book').value.trim();
    const text      = document.getElementById('fb-text').value.trim();

    if (!bookTitle)      { showToast('Please enter the book title.');        return; }
    if (!text)           { showToast('Please write your review.');           return; }
    if (!selectedRating) { showToast('Please select a star rating (1–5).'); return; }

    try {
      await api('POST', '/reviews', {
        username:  currentUser.username,
        category,
        bookTitle,
        rating:    selectedRating,
        text
      });
      document.getElementById('fb-book').value = '';
      document.getElementById('fb-text').value = '';
      selectedRating = 0;
      document.querySelectorAll('.star').forEach(s => s.classList.remove('active'));
      showToast('Review submitted! Thank you 📚');
    } catch (e) {
      showToast('Error: ' + e.message);
    }
  },

  async load() {
    const cat = document.getElementById('filter-cat').value;
    const el  = document.getElementById('reviews-list');
    el.innerHTML =
      '<p style="color:var(--muted);text-align:center;padding:20px">Loading reviews...</p>';

    try {
      const url     = '/reviews' + (cat ? '?cat=' + cat : '');
      const reviews = await api('GET', url);

      if (!reviews || reviews.length === 0) {
        el.innerHTML =
          '<p style="color:var(--muted);text-align:center;padding:40px">' +
          'No reviews yet. Be the first to share!</p>';
        return;
      }

      el.innerHTML = reviews.slice().reverse().map(r => `
        <div class="review-card">
          <div class="rev-hdr">
            <div>
              <div class="rev-name">${r.username}</div>
              <div class="rev-stars">
                ${'★'.repeat(r.rating)}${'☆'.repeat(5 - r.rating)}
              </div>
            </div>
            <span class="stock-badge stock-ok">
              ${CAT_LABELS[r.category] || r.category}
            </span>
          </div>
          <div class="rev-meta">
            📖 ${r.bookTitle}
            ${r.createdAt ? ' · ' + r.createdAt.split(' ')[0] : ''}
          </div>
          <div class="rev-text">${r.text}</div>
        </div>`).join('');

    } catch (e) {
      el.innerHTML =
        '<p style="color:#C0392B;text-align:center;padding:20px">' +
        'Failed to load reviews: ' + e.message + '</p>';
    }
  }
};

// ── ADMIN ─────────────────────────────────────────────────────────────────────
const Admin = {
  async init() {
    try {
      await Promise.all([
        this.loadDashboard(),
        this.loadStock(),
        this.loadOrders(),
        this.loadFeedback(),
        this.loadCustomers()
      ]);
    } catch (e) {
      showToast('Admin init error: ' + e.message);
    }
  },

  async loadDashboard() {
    try {
      const s = await api('GET', '/stats');
      document.getElementById('stat-grid').innerHTML = `
        <div class="stat-card">
          <div class="val">${s.books}</div>
          <div class="lbl">Total Books</div>
        </div>
        <div class="stat-card">
          <div class="val">${s.users}</div>
          <div class="lbl">Customers</div>
        </div>
        <div class="stat-card">
          <div class="val">${s.orders}</div>
          <div class="lbl">Orders</div>
        </div>
        <div class="stat-card">
          <div class="val">LKR ${Number(s.revenue).toLocaleString()}</div>
          <div class="lbl">Revenue</div>
        </div>`;

      const orders = await api('GET', '/orders/all');
      const recent = orders.slice(-5).reverse();
      document.getElementById('recent-wrap').innerHTML = recent.length
        ? `<div class="sec-title" style="font-size:18px;margin:24px 0 14px">
             Recent Orders
           </div>
           <table class="a-table">
             <thead>
               <tr>
                 <th>Order ID</th><th>Customer</th>
                 <th>Total</th><th>Status</th><th>Date</th>
               </tr>
             </thead>
             <tbody>${recent.map(o => `
               <tr>
                 <td>${o.id}</td>
                 <td>${o.username}</td>
                 <td>LKR ${Number(o.total).toLocaleString()}</td>
                 <td>${o.status}</td>
                 <td>${o.createdAt ? o.createdAt.split(' ')[0] : ''}</td>
               </tr>`).join('')}
             </tbody>
           </table>`
        : '<p style="color:var(--muted);margin-top:20px">No orders yet.</p>';
    } catch (e) {
      document.getElementById('stat-grid').innerHTML =
        '<p style="color:#C0392B">Failed to load stats: ' + e.message + '</p>';
    }
  },

  async loadStock() {
    try {
      const books = await api('GET', '/books');
      document.getElementById('stock-wrap').innerHTML = `
        <table class="a-table">
          <thead>
            <tr>
              <th>Title</th><th>Category</th>
              <th>Price</th><th>Stock</th><th>Status</th><th>Action</th>
            </tr>
          </thead>
          <tbody>${books.map(b => {
            const cls = b.stock > 20 ? 'stock-ok' : b.stock > 0 ? 'stock-low' : 'stock-out';
            const lbl = b.stock > 20 ? 'In Stock' : b.stock > 0 ? 'Low Stock' : 'Out of Stock';
            return `
              <tr>
                <td><strong>${b.title}</strong></td>
                <td>${CAT_LABELS[b.cat] || b.cat}</td>
                <td>LKR ${Number(b.price).toLocaleString()}</td>
                <td>
                  <input type="number" id="stk-${b.id}" value="${b.stock}"
                    style="width:70px;padding:5px 8px;
                           border:1.5px solid var(--border);border-radius:4px">
                </td>
                <td><span class="stock-badge ${cls}">${lbl}</span></td>
                <td>
                  <button class="btn btn-primary btn-sm"
                          onclick="Admin.updateStock('${b.id}')">Update</button>
                </td>
              </tr>`;
          }).join('')}
          </tbody>
        </table>`;
    } catch (e) {
      document.getElementById('stock-wrap').innerHTML =
        '<p style="color:#C0392B">Failed to load stock: ' + e.message + '</p>';
    }
  },

  async updateStock(id) {
    const input = document.getElementById('stk-' + id);
    if (!input) return;
    const val = parseInt(input.value);
    if (isNaN(val) || val < 0) { showToast('Invalid stock value.'); return; }
    try {
      await api('PUT', '/books/' + id + '/stock', { stock: val });
      showToast('Stock updated!');
      await this.loadStock();
      await this.loadDashboard();
    } catch (e) {
      showToast('Error: ' + e.message);
    }
  },

  async addBook() {
    const title  = document.getElementById('ab-title').value.trim();
    const author = document.getElementById('ab-author').value.trim();
    const cat    = document.getElementById('ab-cat').value;
    const price  = document.getElementById('ab-price').value;
    const stock  = document.getElementById('ab-stock').value;
    const pages  = document.getElementById('ab-pages').value || '200';
    const img    = document.getElementById('ab-img').value.trim() ||
                   'https://images.unsplash.com/photo-1512820790803-83ca734da794?w=300&h=400&fit=crop';
    const desc   = document.getElementById('ab-desc').value.trim();

    if (!title || !author || !price || !stock) {
      showToast('Please fill in title, author, price and stock.'); return;
    }

    try {
      await api('POST', '/books', {
        cat, title, author,
        price: Number(price),
        pages: Number(pages),
        stock: Number(stock),
        img, desc
      });
      ['ab-title','ab-author','ab-price','ab-stock','ab-pages','ab-img','ab-desc']
        .forEach(id => { document.getElementById(id).value = ''; });
      showToast('Book added! 📚');
      await this.loadStock();
      await this.loadDashboard();
    } catch (e) {
      showToast('Error: ' + e.message);
    }
  },

  async loadOrders() {
    try {
      const orders = await api('GET', '/orders/all');
      document.getElementById('orders-wrap').innerHTML = orders.length
        ? `<table class="a-table">
             <thead>
               <tr>
                 <th>Order ID</th><th>Customer</th>
                 <th>Items</th><th>Total</th><th>Status</th><th>Date</th>
               </tr>
             </thead>
             <tbody>${orders.slice().reverse().map(o => `
               <tr>
                 <td>${o.id}</td>
                 <td>${o.username}</td>
                 <td>${(o.items || []).length} item(s)</td>
                 <td>LKR ${Number(o.total).toLocaleString()}</td>
                 <td>${o.status}</td>
                 <td>${o.createdAt ? o.createdAt.split(' ')[0] : ''}</td>
               </tr>`).join('')}
             </tbody>
           </table>`
        : '<p style="color:var(--muted)">No orders yet.</p>';
    } catch (e) {
      document.getElementById('orders-wrap').innerHTML =
        '<p style="color:#C0392B">Failed to load orders: ' + e.message + '</p>';
    }
  },

  async loadFeedback() {
    try {
      const reviews = await api('GET', '/reviews');
      document.getElementById('feedback-wrap').innerHTML = reviews.length
        ? reviews.slice().reverse().map(r => `
            <div class="review-card" style="margin-bottom:14px">
              <div class="rev-hdr">
                <div>
                  <div class="rev-name">${r.username}</div>
                  <div class="rev-stars">
                    ${'★'.repeat(r.rating)}${'☆'.repeat(5 - r.rating)}
                  </div>
                </div>
                <span class="stock-badge stock-ok">
                  ${CAT_LABELS[r.category] || r.category}
                </span>
              </div>
              <div class="rev-meta">📖 ${r.bookTitle}</div>
              <div class="rev-text">${r.text}</div>
            </div>`).join('')
        : '<p style="color:var(--muted)">No reviews yet.</p>';
    } catch (e) {
      document.getElementById('feedback-wrap').innerHTML =
        '<p style="color:#C0392B">Failed to load feedback: ' + e.message + '</p>';
    }
  },

  async loadCustomers() {
    try {
      const users = await api('GET', '/users/all');
      document.getElementById('customers-wrap').innerHTML = users.length
        ? `<table class="a-table">
             <thead>
               <tr>
                 <th>Name</th><th>Username</th>
                 <th>Email</th><th>Joined</th><th>Action</th>
               </tr>
             </thead>
             <tbody>${users.map(u => `
               <tr>
                 <td>${u.name}</td>
                 <td>${u.username}</td>
                 <td>${u.email}</td>
                 <td>${u.createdAt ? u.createdAt.split(' ')[0] : ''}</td>
                 <td>
                   <button class="btn btn-danger btn-sm"
                           onclick="Admin.deleteUser('${u.username}')">
                     Delete
                   </button>
                 </td>
               </tr>`).join('')}
             </tbody>
           </table>`
        : '<p style="color:var(--muted)">No customers registered yet.</p>';
    } catch (e) {
      document.getElementById('customers-wrap').innerHTML =
        '<p style="color:#C0392B">Failed to load customers: ' + e.message + '</p>';
    }
  },

  async deleteUser(username) {
    if (!confirm('Delete account "' + username + '"? This cannot be undone.')) return;
    try {
      await api('DELETE', '/users/' + username);
      showToast('Customer deleted.');
      await this.loadCustomers();
      await this.loadDashboard();
    } catch (e) {
      showToast('Error: ' + e.message);
    }
  }
};

// ── UI HELPERS ────────────────────────────────────────────────────────────────
function showPage(id) {
  document.querySelectorAll('.page').forEach(p => p.classList.remove('active'));
  const page = document.getElementById(id);
  if (page) {
    page.classList.add('active');
    window.scrollTo(0, 0);
  }
  // When switching to user page, always reset to home section
  if (id === 'page-user') {
    showSection('home');
  }
}

function showSection(name) {
  // Scope to #page-user only — prevents touching admin or other page sections
  const pageUser = document.getElementById('page-user');
  if (pageUser) {
    pageUser.querySelectorAll('.section').forEach(s => s.classList.remove('active'));
  }
  const map = {
    home:     'sec-home',
    books:    'sec-books',
    cart:     'sec-cart',
    feedback: 'sec-feedback',
    profile:  'sec-profile'
  };
  const el = document.getElementById(map[name]);
  if (el) {
    el.classList.add('active');
  } else {
    console.error('showSection: unknown section', name);
  }

  if (name === 'cart')    Cart.render();
  if (name === 'profile') renderProfile();
}

function adminTab(name, btn) {
  document.querySelectorAll('.a-sec').forEach(s => s.classList.remove('active'));
  document.getElementById('a-' + name).classList.add('active');
  document.querySelectorAll('.sb-btn').forEach(b => b.classList.remove('active'));
  if (btn) btn.classList.add('active');
}

function switchTab(tab) {
  const isWrite = tab === 'write';
  document.querySelectorAll('.tab').forEach((t, i) =>
    t.classList.toggle('active', i === (isWrite ? 0 : 1)));
  document.getElementById('tab-write').classList.toggle('hidden',  !isWrite);
  document.getElementById('tab-read').classList.toggle('hidden',    isWrite);
  if (!isWrite) Feedback.load();
}

async function renderProfile() {
  const el = document.getElementById('profile-card');
  el.innerHTML =
    '<div style="padding:20px;color:var(--muted)">Loading profile...</div>';
  try {
    const orders = await api('GET', '/orders?user=' + currentUser.username);
    el.innerHTML = `
      <div class="avatar">${currentUser.name[0].toUpperCase()}</div>
      <h3 style="font-family:'Playfair Display',serif;font-size:22px;margin-bottom:4px">
        ${currentUser.name}
      </h3>
      <p style="color:var(--muted);font-size:14px;margin-bottom:20px">
        @${currentUser.username}
      </p>
      <div style="background:var(--cream);border-radius:8px;padding:16px;margin-bottom:20px">
        <div class="info-row">
          <span style="color:var(--muted);font-size:13px">Username</span>
          <span style="font-weight:600">${currentUser.username}</span>
        </div>
        <div class="info-row">
          <span style="color:var(--muted);font-size:13px">Orders Placed</span>
          <span style="font-weight:600">${orders.length}</span>
        </div>
        <div class="info-row" style="border-bottom:none">
          <span style="color:var(--muted);font-size:13px">Role</span>
          <span style="font-weight:600">${currentUser.role}</span>
        </div>
      </div>
      <div class="danger-zone">
        <h4>⚠️ Danger Zone</h4>
        <button class="btn btn-danger" onclick="deleteMyAccount()">
          Delete My Account
        </button>
      </div>`;
  } catch (e) {
    el.innerHTML =
      '<div style="color:#C0392B;padding:20px">Failed to load profile: ' +
      e.message + '</div>';
  }
}

async function deleteMyAccount() {
  if (!confirm('Permanently delete your account? This cannot be undone.')) return;
  try {
    await api('DELETE', '/users/' + currentUser.username);
    Auth.logout();
    showToast('Your account has been deleted.');
  } catch (e) {
    showToast('Error: ' + e.message);
  }
}

function showMsg(el, type, text) {
  el.className     = 'msg ' + type;
  el.textContent   = text;
  el.style.display = 'block';
}
function hideMsg(el) {
  el.style.display = 'none';
  el.className     = 'msg';
}

function showToast(msg) {
  const t = document.getElementById('toast');
  t.textContent = msg;
  t.classList.add('show');
  setTimeout(() => t.classList.remove('show'), 3000);
}

// ── INIT ──────────────────────────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {

  // Star rating clicks
  document.querySelectorAll('.star').forEach(star => {
    star.addEventListener('click', () => {
      selectedRating = parseInt(star.dataset.v);
      document.querySelectorAll('.star').forEach((s, i) =>
        s.classList.toggle('active', i < selectedRating));
    });
  });

  // Close modal by clicking the dark overlay
  document.getElementById('modal').addEventListener('click', e => {
    if (e.target === document.getElementById('modal')) closeModal();
  });

  // Allow Enter key on login fields
  document.getElementById('login-pass').addEventListener('keydown', e => {
    if (e.key === 'Enter') Auth.login();
  });
  document.getElementById('login-user').addEventListener('keydown', e => {
    if (e.key === 'Enter') Auth.login();
  });

  console.log('PageTurner frontend loaded. Server should be at http://localhost:8080');
});
