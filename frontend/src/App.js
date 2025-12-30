/**
 * @fileoverview Main Application Component for Calorie Tracker
 * 
 * This is the root component that sets up:
 * - Material-UI theming with custom color palette
 * - React Router for client-side navigation
 * - Authentication context provider for global auth state
 * - Route protection (PrivateRoute/PublicRoute components)
 * 
 * @description A full-stack calorie tracking application built with React 18,
 * Material-UI, and React Router. Features include daily calorie tracking,
 * meal logging, weight monitoring, and AI-powered nutritional advice.
 * 
 * @version 1.0.0
 * @author Calorie Tracker Team
 */

import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import { AuthProvider, useAuth } from './context/AuthContext';
import Login from './components/auth/Login';
import Register from './components/auth/Register';
import Dashboard from './components/dashboard/Dashboard';
import Foods from './components/foods/Foods';
import History from './components/history/History';
import WeightTracking from './components/weight/WeightTracking';
import Profile from './components/profile/Profile';
import Advice from './components/advice/Advice';
import Plan from './components/plan/Plan';
import Layout from './components/layout/Layout';

/**
 * Material-UI theme configuration
 * Defines the application's color scheme, typography, and component defaults
 */
const theme = createTheme({
  palette: {
    primary: {
      main: '#1976d2', // Blue - primary brand color
    },
    secondary: {
      main: '#dc004e', // Pink/Red - accent color
    },
    background: {
      default: '#f5f5f5', // Light gray background
    },
  },
  typography: {
    fontFamily: '"Roboto", "Helvetica", "Arial", sans-serif',
  },
});

/**
 * PrivateRoute - Route guard component for authenticated routes
 * 
 * Protects routes that require authentication. If user is not authenticated,
 * redirects to login page. Shows loading state while auth status is being determined.
 * 
 * @param {Object} props - Component props
 * @param {React.ReactNode} props.children - Child components to render if authenticated
 * @returns {React.ReactNode} Protected route content or redirect
 */
const PrivateRoute = ({ children }) => {
  const { isAuthenticated, loading } = useAuth();
  
  if (loading) {
    return <div>Loading...</div>;
  }
  
  return isAuthenticated ? children : <Navigate to="/login" />;
};

/**
 * PublicRoute - Route guard component for public-only routes
 * 
 * Protects routes that should only be accessible to non-authenticated users
 * (login, register). Redirects authenticated users to dashboard.
 * 
 * @param {Object} props - Component props
 * @param {React.ReactNode} props.children - Child components to render if not authenticated
 * @returns {React.ReactNode} Public route content or redirect
 */
const PublicRoute = ({ children }) => {
  const { isAuthenticated, loading } = useAuth();
  
  if (loading) {
    return <div>Loading...</div>;
  }
  
  return !isAuthenticated ? children : <Navigate to="/dashboard" />;
};

/**
 * App - Root application component
 * 
 * Sets up the application structure with:
 * - ThemeProvider for Material-UI styling
 * - AuthProvider for authentication state management
 * - Router for client-side navigation
 * - Route definitions with appropriate guards
 * 
 * @returns {React.ReactElement} The complete application
 */
function App() {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <AuthProvider>
        <Router>
          <Routes>
            <Route path="/login" element={
              <PublicRoute>
                <Login />
              </PublicRoute>
            } />
            <Route path="/register" element={
              <PublicRoute>
                <Register />
              </PublicRoute>
            } />
            <Route path="/" element={
              <PrivateRoute>
                <Layout />
              </PrivateRoute>
            }>
              <Route index element={<Navigate to="/dashboard" replace />} />
              <Route path="dashboard" element={<Dashboard />} />
              <Route path="foods" element={<Foods />} />
              <Route path="history" element={<History />} />
              <Route path="weight" element={<WeightTracking />} />
              <Route path="advice" element={<Advice />} />
              <Route path="plan" element={<Plan />} />
              <Route path="profile" element={<Profile />} />
            </Route>
            <Route path="*" element={<Navigate to="/dashboard" replace />} />
          </Routes>
        </Router>
      </AuthProvider>
    </ThemeProvider>
  );
}

export default App;
