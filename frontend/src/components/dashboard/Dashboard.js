import React, { useState, useEffect } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Grid,
  LinearProgress,
  CircularProgress,
  Alert,
} from '@mui/material';
import { useAuth } from '../../context/AuthContext';
import { dashboardService } from '../../services/api';

const Dashboard = () => {
  const { user } = useAuth();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [dashboardData, setDashboardData] = useState({
    allowedDailyIntake: user?.allowedDailyIntake || 2000,
    consumedCalories: 0,
    remainingCalories: user?.allowedDailyIntake || 2000,
    percentageConsumed: 0,
    mealsByType: {},
    totalMealsCount: 0,
  });

  useEffect(() => {
    const fetchDashboardData = async () => {
      try {
        setLoading(true);
        setError('');
        const response = await dashboardService.getTodaySummary();
        const data = response.data;
        
        const consumed = data.consumedCalories || 0;
        const allowed = data.allowedDailyIntake || user?.allowedDailyIntake || 2000;
        
        setDashboardData({
          allowedDailyIntake: allowed,
          consumedCalories: consumed,
          remainingCalories: data.remainingCalories ?? (allowed - consumed),
          percentageConsumed: allowed > 0 ? (consumed / allowed) * 100 : 0,
          mealsByType: data.mealsByType || {},
          totalMealsCount: data.totalMealsCount || 0,
          userName: data.userName,
          goalType: data.goalType,
          currentWeight: data.currentWeight,
          goalWeight: data.goalWeight,
          todayWeight: data.todayWeight,
        });
      } catch (err) {
        console.error('Failed to fetch dashboard data:', err);
        setError('Failed to load dashboard data. Please try again.');
        // Fallback to user data from context
        setDashboardData({
          allowedDailyIntake: user?.allowedDailyIntake || 2000,
          consumedCalories: 0,
          remainingCalories: user?.allowedDailyIntake || 2000,
          percentageConsumed: 0,
          mealsByType: {},
          totalMealsCount: 0,
        });
      } finally {
        setLoading(false);
      }
    };

    fetchDashboardData();
  }, [user]);

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="50vh">
        <CircularProgress />
      </Box>
    );
  }

  const getProgressColor = (percentage) => {
    if (percentage < 50) return 'success';
    if (percentage < 80) return 'warning';
    return 'error';
  };

  return (
    <Box>
      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}
      <Typography variant="h4" gutterBottom>
        Welcome, {dashboardData.userName || user?.name || 'User'}!
      </Typography>
      <Typography variant="h5" gutterBottom>
        Today's Summary
      </Typography>
      <Typography variant="subtitle1" color="textSecondary" gutterBottom>
        {new Date().toLocaleDateString('en-US', { 
          weekday: 'long', 
          year: 'numeric', 
          month: 'long', 
          day: 'numeric' 
        })}
      </Typography>

      <Grid container spacing={3} sx={{ mt: 2 }}>
        <Grid item xs={12} md={4}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>
                Daily Allowance
              </Typography>
              <Typography variant="h4" component="div">
                {dashboardData.allowedDailyIntake}
              </Typography>
              <Typography color="textSecondary">
                calories
              </Typography>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} md={4}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>
                Consumed
              </Typography>
              <Typography variant="h4" component="div" color="primary">
                {dashboardData.consumedCalories}
              </Typography>
              <Typography color="textSecondary">
                calories
              </Typography>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} md={4}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>
                Remaining
              </Typography>
              <Typography 
                variant="h4" 
                component="div"
                color={dashboardData.remainingCalories >= 0 ? 'success.main' : 'error.main'}
              >
                {dashboardData.remainingCalories}
              </Typography>
              <Typography color="textSecondary">
                calories
              </Typography>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>
                Daily Progress
              </Typography>
              <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                <Box sx={{ width: '100%', mr: 1 }}>
                  <LinearProgress 
                    variant="determinate" 
                    value={Math.min(dashboardData.percentageConsumed, 100)} 
                    color={getProgressColor(dashboardData.percentageConsumed)}
                    sx={{ height: 20, borderRadius: 5 }}
                  />
                </Box>
                <Box sx={{ minWidth: 50 }}>
                  <Typography variant="body2" color="textSecondary">
                    {dashboardData.percentageConsumed.toFixed(1)}%
                  </Typography>
                </Box>
              </Box>
              <Typography variant="body2" color="textSecondary">
                {dashboardData.consumedCalories} of {dashboardData.allowedDailyIntake} calories consumed
              </Typography>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Today's Meals
              </Typography>
              <Typography color="textSecondary">
                No meals logged yet. Start tracking your food intake!
              </Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
};

export default Dashboard;
