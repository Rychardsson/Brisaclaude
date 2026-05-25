import axios from 'axios';

const DEFAULT_API_URL = 'http://localhost:8082/api';
const API_URL = (import.meta.env.VITE_API_BASE_URL || DEFAULT_API_URL).replace(/\/$/, '');

const publicApi = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json'
  }
});

export default publicApi;
