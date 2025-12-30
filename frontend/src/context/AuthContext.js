/**
 * @fileoverview Authentication Context for Calorie Tracker
 * 
 * This module implements React Context API for global authentication state management.
 * It provides:
 * - Authentication state (user data, loading status)
 * - Login/Register/Logout functions
 * - Persistent authentication via localStorage
 * - Custom useAuth hook for easy access to auth state
 * 
 * @module context/AuthContext
 * @version 1.0.0
 * @author Calorie Tracker Team
 */

import React, { createContext, useContext, useState, useEffect } from 'react';
import { authService } from '../services/api';

/** React Context for authentication state */
const AuthContext = createContext(null);

/**
 * Custom hook to access authentication context
 * 
 * Provides access to user state and auth functions (login, register, logout).
 * Must be used within an AuthProvider component.
 * 
 * @returns {Object} Authentication context value
 * @throws {Error} If used outside of AuthProvider
 * 
 * @example
 * const { user, isAuthenticated, login, logout } = useAuth();
 */
export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

/**
 * AuthProvider Component
 * 
 * Provides authentication context to the entire application.
 * Handles:
 * - Initial auth state restoration from localStorage
 * - Login with JWT token storage
 * - Registration with automatic login
 * - Logout with credential cleanup
 * 
 * @param {Object} props - Component props
 * @param {React.ReactNode} props.children - Child components
 * @returns {React.ReactElement} Provider component wrapping children
 */
export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const storedUser = localStorage.getItem('user');
    const storedToken = localStorage.getItem('token');
    
    if (storedUser && storedToken) {
      setUser(JSON.parse(storedUser));
    }
    setLoading(false);
  }, []);

  /**
   * Authenticates user with email and password
   * 
   * On success, stores JWT token and user data in localStorage,
   * updates context state, and returns success status.
   * 
   * @param {string} email - User's email address
   * @param {string} password - User's password
   * @returns {Promise<{success: boolean, message?: string}>} Login result
   */
  const login = async (email, password) => {
    try {
      const response = await authService.login({ email, password });
      const data = response.data;
      
      // Extract user info from response
      const userData = {
        userId: data.userId,
        name: data.name,
        email: data.email,
        bmi: data.bmi,
        allowedDailyIntake: data.allowedDailyIntake,
      };
      
      // Persist credentials to localStorage for session persistence
      localStorage.setItem('token', data.token);
      localStorage.setItem('user', JSON.stringify(userData));
      setUser(userData);
      
      return { success: true };
    } catch (error) {
      console.log(error);
      return { 
        success: false, 
        message: error.response?.data?.message || 'Login failed' 
      };
    }
  };

  /**
   * Registers a new user account
   * 
   * Creates new user with provided registration data, automatically
   * logs in the user on success by storing credentials.
   * 
   * @param {Object} registrationData - User registration data
   * @param {string} registrationData.name - User's full name
   * @param {string} registrationData.email - User's email address
   * @param {string} registrationData.password - User's password
   * @param {string} registrationData.dob - Date of birth
   * @param {string} registrationData.sex - Biological sex (MALE/FEMALE)
   * @param {number} registrationData.weight - Weight in kg
   * @param {number} registrationData.height - Height in cm
   * @param {string} registrationData.activityLevel - Activity level
   * @param {number} registrationData.goal - Target weight
   * @param {number} registrationData.weeklyGoal - Weekly weight change goal
   * @returns {Promise<{success: boolean, message?: string}>} Registration result
   */
  const register = async (registrationData) => {
    try {
      const response = await authService.register(registrationData);
      const data = response.data;
      
      // Extract user info from response
      const userData = {
        userId: data.userId,
        name: data.name,
        email: data.email,
        bmi: data.bmi,
        allowedDailyIntake: data.allowedDailyIntake,
      };
      
      // Persist credentials to localStorage for session persistence
      localStorage.setItem('token', data.token);
      localStorage.setItem('user', JSON.stringify(userData));
      setUser(userData);
      
      return { success: true };
    } catch (error) {
      console.log(error);
      return { 
        success: false, 
        message: error.response?.data?.message || 'Registration failed' 
      };
    }
  };

  /**
   * Logs out the current user
   * 
   * Clears all stored credentials from localStorage and resets
   * the user state to null, effectively ending the session.
   */
  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setUser(null);
  };

  const value = {
    user,
    isAuthenticated: !!user,
    loading,
    login,
    register,
    logout,
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};
