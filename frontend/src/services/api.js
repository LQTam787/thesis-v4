import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const isAuthEndpoint = error.config?.url?.startsWith('/auth/');
    if (error.response?.status === 401 && !isAuthEndpoint) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export const authService = {
  login: (credentials) => api.post('/auth/login', credentials),
  register: (userData) => api.post('/auth/register', userData),
};

export const dashboardService = {
  getTodaySummary: () => api.get('/dashboard'),
  getDashboardByDate: (date) => api.get(`/dashboard/date/${date}`),
};

export const foodService = {
  getAllFoods: () => api.get('/foods'),
  getFoodsByMealType: (mealType) => api.get(`/foods/meal-type/${mealType}`),
  getFoodById: (id) => api.get(`/foods/${id}`),
  getCustomFoods: () => api.get('/foods/custom'),
  addFood: (foodData) => api.post('/foods', foodData),
  updateFood: (id, foodData) => api.put(`/foods/${id}`, foodData),
  deleteFood: (id) => api.delete(`/foods/${id}`),
};

export const mealEntryService = {
  logMeal: (mealData) => api.post('/meal-entries', mealData),
  getMealsByDate: (date) => api.get(`/meal-entries/date/${date}`),
  deleteMealEntry: (id) => api.delete(`/meal-entries/${id}`),
};

export const weightEntryService = {
  logWeight: (weightData) => api.post('/weight-entries', weightData),
  getAllWeightEntries: () => api.get('/weight-entries'),
  getWeightEntriesByRange: (startDate, endDate) => 
    api.get(`/weight-entries/range?startDate=${startDate}&endDate=${endDate}`),
  getLatestWeightEntry: () => api.get('/weight-entries/latest'),
  deleteWeightEntry: (id) => api.delete(`/weight-entries/${id}`),
};

export const userService = {
  getProfile: () => api.get('/users/profile'),
  updateProfile: (profileData) => api.put('/users/profile', profileData),
};

export const adviceService = {
  chat: (message, history) => api.post('/advice/chat', { message, history }),
};

export default api;
