import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add token to requests
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Handle auth errors
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// Auth API
export const authAPI = {
  register: (data) => api.post('/auth/register', data),
  login: (data) => api.post('/auth/login', data),
};

// Projects API
export const projectsAPI = {
  getAll: () => api.get('/projects'),
  getById: (id) => api.get(`/projects/${id}`),
  create: (data) => api.post('/projects', data),
  update: (id, data) => api.put(`/projects/${id}`, data),
  delete: (id) => api.delete(`/projects/${id}`),
};

// Modules API
export const modulesAPI = {
  getByProject: (projectId) => api.get(`/projects/${projectId}/modules`),
  getById: (id) => api.get(`/modules/${id}`),
  create: (projectId, data) => api.post(`/projects/${projectId}/modules`, data),
  update: (id, data) => api.put(`/modules/${id}`, data),
  delete: (id) => api.delete(`/modules/${id}`),
  getDevelopers: (moduleId) => api.get(`/modules/${moduleId}/developers`),
  mapDeveloper: (moduleId, developerId) => 
    api.post(`/modules/${moduleId}/developers`, { developerId }),
  unmapDeveloper: (moduleId, developerId) => 
    api.delete(`/modules/${moduleId}/developers/${developerId}`),
};

// Developers API
export const developersAPI = {
  getAll: () => api.get('/developers'),
  getById: (id) => api.get(`/developers/${id}`),
  getMyProfile: () => api.get('/developers/me'),
  getMyBugs: (params) => api.get('/developers/me/bugs', { params }),
  getMetrics: (id) => api.get(`/developers/${id}/metrics`),
  getWorkload: (id) => api.get(`/developers/${id}/workload`),
  getLeaderboard: (params) => api.get('/developers/leaderboard', { params }),
  getAssignmentHistory: (id) => api.get(`/developers/${id}/assignment-history`),
  create: (data) => api.post('/developers', data),
  update: (id, data) => api.put(`/developers/${id}`, data),
  linkUser: (id, userId) => api.put(`/developers/${id}/link-user`, { userId }),
  delete: (id) => api.delete(`/developers/${id}`),
};

// Bugs API
export const bugsAPI = {
  getByProject: (projectId, params) => api.get(`/projects/${projectId}/bugs`, { params }),
  getSlaSummary: (projectId) => api.get(`/projects/${projectId}/bugs/sla-summary`),
  getById: (id) => api.get(`/bugs/${id}`),
  getAssignmentHistory: (id) => api.get(`/bugs/${id}/assignment-history`),
  create: (projectId, moduleId, data) => api.post(`/projects/${projectId}/modules/${moduleId}/bugs`, data),
  update: (id, data) => api.put(`/bugs/${id}`, data),
  assign: (id, developerId) => api.patch(`/bugs/${id}/assign`, { developerId }),
  unassign: (id) => api.patch(`/bugs/${id}/unassign`),
  changeStatus: (id, status, resolutionNotes) => api.patch(`/bugs/${id}/status`, { status, resolutionNotes }),
  delete: (id) => api.delete(`/bugs/${id}`),
};

// SLA Rules API
export const slaRulesAPI = {
  getAll: () => api.get('/sla-rules'),
  getById: (id) => api.get(`/sla-rules/${id}`),
  update: (id, data) => api.put(`/sla-rules/${id}`, data),
};

export default api;