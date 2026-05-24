import api from './api';

function decodeTokenPayload(token) {
  try {
    const payload = String(token || '').split('.')[1];
    if (!payload) return null;

    const base64 = payload.replace(/-/g, '+').replace(/_/g, '/');
    const padded = base64.padEnd(Math.ceil(base64.length / 4) * 4, '=');
    const json = decodeURIComponent(
      atob(padded)
        .split('')
        .map((char) => `%${char.charCodeAt(0).toString(16).padStart(2, '0')}`)
        .join('')
    );

    return JSON.parse(json);
  } catch {
    return null;
  }
}

function buildStoredUser(data) {
  const claims = decodeTokenPayload(data?.token);

  if (!claims) {
    return data;
  }

  return {
    ...data,
    ...claims,
    id: claims.id || data?.id || null,
    login: claims.sub || data?.login || '',
    role: String(claims.role || data?.role || '').toUpperCase()
  };
}

export const authService = {
  async login(login, password) {
    try {
      const response = await api.post('/auth/login', { login, password });
      if (response.data.token) {
        const storedUser = buildStoredUser(response.data);
        localStorage.setItem('token', response.data.token);
        localStorage.setItem('user', JSON.stringify(storedUser));
      }
      return response.data;
    } catch (error) {
      throw error;
    }
  },

  logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  },

  getToken() {
    return localStorage.getItem('token');
  },

  isAuthenticated() {
    return !!this.getToken();
  },

  getUser() {
    const user = localStorage.getItem('user');
    const parsedUser = user ? JSON.parse(user) : null;

    if (parsedUser?.role) {
      return parsedUser;
    }

    const token = parsedUser?.token || this.getToken();
    const fallbackUser = token ? buildStoredUser({ ...(parsedUser || {}), token }) : parsedUser;

    if (fallbackUser?.role) {
      localStorage.setItem('user', JSON.stringify(fallbackUser));
    }

    return fallbackUser;
  }
};
