import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
});

export const getProblems = () => api.get('/problems').then(res => res.data);
export const getProblem = (id) => api.get(`/problems/${id}`).then(res => res.data);
export const createAttempt = (problemId, format, content) => api.post(`/problems/${problemId}/attempts`, { format, content }).then(res => res.data);
export const getAttempt = (id) => api.get(`/attempts/${id}`).then(res => res.data);
export const getAttempts = (problemId) => {
  const url = problemId ? `/attempts?problemId=${problemId}` : '/attempts';
  return api.get(url).then(res => res.data);
};

export default api;
