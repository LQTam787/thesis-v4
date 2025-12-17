import React, { useState, useEffect } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Grid,
  LinearProgress,
  CircularProgress,
} from '@mui/material';
import { useAuth } from '../../context/AuthContext';

const Dashboard = () => {
  const { user } = useAuth();
  const [loading, setLoading] = useState(false);
  const [dashboardData, setDashboardData] = useState({
    allowedDailyIntake: user?.allowedDailyIntake || 2000,
    consumedCalories: 0,
    remainingCalories: user?.allowedDailyIntake || 2000,
    percentageConsumed: 0,
  });

  useEffect(() => {
    // TODO: Fetch dashboard data from API
    // For now, using placeholder data
    setDashboardData({
      allowedDailyIntake: user?.allowedDailyIntake || 2000,
      consumedCalories: 0,
      remainingCalories: user?.allowedDailyIntake || 2000,
      percentageConsumed: 0,
    });
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
      <Typography variant="h4" gutterBottom>
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
