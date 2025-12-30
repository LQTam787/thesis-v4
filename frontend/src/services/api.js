/**
 * @fileoverview API Service Module for Calorie Tracker Frontend
 * 
 * This module provides a centralized API client using Axios for all HTTP
 * communications with the Spring Boot backend. It includes:
 * - Axios instance configuration with base URL and headers
 * - Request interceptor for JWT token injection
 * - Response interceptor for automatic 401 handling
 * - Service objects for each API domain (auth, dashboard, foods, etc.)
 * 
 * @module services/api
 * @version 1.0.0
 * @author Calorie Tracker Team
 */

import axios from 'axios';

/** Base URL for all API requests - points to Spring Boot backend */
const API_BASE_URL = 'http://localhost:8080/api';

/**
 * Configured Axios instance for API requests
 * Pre-configured with base URL and JSON content type
 */
const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

/**
 * Request Interceptor - Adds JWT token to Authorization header
 * 
 * Automatically retrieves the JWT token from localStorage and adds it
 * to every outgoing request as a Bearer token. This enables stateless
 * authentication with the backend.
 */
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

/**
 * Response Interceptor - Handles authentication errors
 * 
 * Automatically handles 401 Unauthorized responses by clearing stored
 * credentials and redirecting to login page. Excludes auth endpoints
 * to prevent redirect loops during login/register.
 */
api.interceptors.response.use(
  (response) => response,
  (error) => {
    const isAuthEndpoint = error.config?.url?.startsWith('/auth/');
    if (error.response?.status === 401 && !isAuthEndpoint) {
      // Clear stored credentials on authentication failure
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      // Redirect to login page
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

/**
 * Authentication Service
 * Handles user login and registration
 */
export const authService = {
  /** Authenticate user with email and password */
  login: (credentials) => api.post('/auth/login', credentials),
  /** Register new user account */
  register: (userData) => api.post('/auth/register', userData),
};

/**
 * Dashboard Service
 * Retrieves aggregated daily summary data
 */
export const dashboardService = {
  /** Get today's dashboard summary */
  getTodaySummary: () => api.get('/dashboard'),
  /** Get dashboard data for a specific date */
  getDashboardByDate: (date) => api.get(`/dashboard/date/${date}`),
};

/**
 * Food Service
 * CRUD operations for food items (system and custom)
 */
export const foodService = {
  /** Get all available foods (system + user's custom) */
  getAllFoods: () => api.get('/foods'),
  /** Get foods filtered by meal type */
  getFoodsByMealType: (mealType) => api.get(`/foods/meal-type/${mealType}`),
  /** Get single food by ID */
  getFoodById: (id) => api.get(`/foods/${id}`),
  /** Get only user's custom foods */
  getCustomFoods: () => api.get('/foods/custom'),
  /** Create new custom food */
  addFood: (foodData) => api.post('/foods', foodData),
  /** Update existing custom food */
  updateFood: (id, foodData) => api.put(`/foods/${id}`, foodData),
  /** Delete custom food */
  deleteFood: (id) => api.delete(`/foods/${id}`),
};

/**
 * Meal Entry Service
 * Handles meal logging and calorie tracking
 */
export const mealEntryService = {
  /** Log a new meal entry */
  logMeal: (mealData) => api.post('/meal-entries', mealData),
  /** Get meal entries for a specific date */
  getMealsByDate: (date) => api.get(`/meal-entries/date/${date}`),
  /** Delete a meal entry */
  deleteMealEntry: (id) => api.delete(`/meal-entries/${id}`),
};

/**
 * Weight Entry Service
 * Handles weight tracking and progress monitoring
 */
export const weightEntryService = {
  /** Log or update weight for a date (upsert) */
  logWeight: (weightData) => api.post('/weight-entries', weightData),
  /** Get all weight entries for charts */
  getAllWeightEntries: () => api.get('/weight-entries'),
  /** Get weight entries within date range */
  getWeightEntriesByRange: (startDate, endDate) => 
    api.get(`/weight-entries/range?startDate=${startDate}&endDate=${endDate}`),
  /** Get most recent weight entry */
  getLatestWeightEntry: () => api.get('/weight-entries/latest'),
  /** Delete a weight entry */
  deleteWeightEntry: (id) => api.delete(`/weight-entries/${id}`),
};

/**
 * User Service
 * Handles user profile management
 */
export const userService = {
  /** Get current user's profile */
  getProfile: () => api.get('/users/profile'),
  /** Update user profile (triggers BMI/calorie recalculation) */
  updateProfile: (profileData) => api.put('/users/profile', profileData),
};

/**
 * Advice Service
 * AI-powered diet advice chatbot
 */
export const adviceService = {
  /** Send message to AI advisor and get response */
  chat: (message, history) => api.post('/advice/chat', { message, history }),
};

/**
 * Plan Service
 * AI-powered meal plan generation
 */
export const planService = {
  /** Get current meal plan */
  getPlan: () => api.get('/plan'),
  /** Generate new AI meal plan */
  generatePlan: () => api.post('/plan/generate'),
};

/**
 * Review Service
 * AI-powered progress review generation
 */
export const reviewService = {
  /** Get current progress review */
  getReview: () => api.get('/review'),
  /** Generate new AI progress review */
  generateReview: () => api.post('/review/generate'),
};

export default api;
