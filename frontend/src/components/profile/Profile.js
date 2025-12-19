import React, { useState, useEffect } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Grid,
  CircularProgress,
  Alert,
  Divider,
  Chip,
} from '@mui/material';
import PersonIcon from '@mui/icons-material/Person';
import FitnessCenterIcon from '@mui/icons-material/FitnessCenter';
import MonitorWeightIcon from '@mui/icons-material/MonitorWeight';
import { userService } from '../../services/api';

const Profile = () => {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [profile, setProfile] = useState(null);

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        setLoading(true);
        setError('');
        const response = await userService.getProfile();
        setProfile(response.data);
      } catch (err) {
        console.error('Failed to fetch profile:', err);
        setError('Failed to load profile data. Please try again.');
      } finally {
        setLoading(false);
      }
    };

    fetchProfile();
  }, []);

  const formatDate = (dateString) => {
    if (!dateString) return '-';
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  };

  const formatActivityLevel = (level) => {
    if (!level) return '-';
    const labels = {
      SEDENTARY: 'Sedentary (little to no exercise)',
      LIGHTLY_ACTIVE: 'Lightly Active (1-3 days/week)',
      MODERATELY_ACTIVE: 'Moderately Active (3-5 days/week)',
      VERY_ACTIVE: 'Very Active (6-7 days/week)',
    };
    return labels[level] || level;
  };

  const formatGoalType = (type) => {
    if (!type) return '-';
    const labels = {
      LOSE: 'Lose Weight',
      MAINTAIN: 'Maintain Weight',
      GAIN: 'Gain Weight',
    };
    return labels[type] || type;
  };

  const formatSex = (sex) => {
    if (!sex) return '-';
    return sex.charAt(0) + sex.slice(1).toLowerCase();
  };

  const getBmiCategory = (bmi) => {
    if (!bmi) return { label: '-', color: 'default' };
    const value = parseFloat(bmi);
    if (value < 18.5) return { label: 'Underweight', color: 'info' };
    if (value < 25) return { label: 'Normal', color: 'success' };
    if (value < 30) return { label: 'Overweight', color: 'warning' };
    return { label: 'Obese', color: 'error' };
  };

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="50vh">
        <CircularProgress />
      </Box>
    );
  }

  if (error) {
    return (
      <Box sx={{ p: 2 }}>
        <Alert severity="error">{error}</Alert>
      </Box>
    );
  }

  const bmiInfo = getBmiCategory(profile?.bmi);

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        My Profile
      </Typography>

      <Grid container spacing={3}>
        {/* Profile Section */}
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                <PersonIcon sx={{ mr: 1, color: 'primary.main' }} />
                <Typography variant="h6">Profile</Typography>
              </Box>
              <Divider sx={{ mb: 2 }} />

              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                  <Typography color="textSecondary">Name</Typography>
                  <Typography fontWeight="medium">{profile?.name || '-'}</Typography>
                </Box>

                <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                  <Typography color="textSecondary">Email</Typography>
                  <Typography fontWeight="medium">{profile?.email || '-'}</Typography>
                </Box>

                <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                  <Typography color="textSecondary">Sex</Typography>
                  <Typography fontWeight="medium">{formatSex(profile?.sex)}</Typography>
                </Box>

                <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                  <Typography color="textSecondary">Birthdate</Typography>
                  <Typography fontWeight="medium">{formatDate(profile?.dob)}</Typography>
                </Box>

                <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                  <Typography color="textSecondary">Height</Typography>
                  <Typography fontWeight="medium">
                    {profile?.height ? `${profile.height} cm` : '-'}
                  </Typography>
                </Box>

                <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                  <Typography color="textSecondary">Activity Level</Typography>
                  <Typography fontWeight="medium" sx={{ textAlign: 'right', maxWidth: '60%' }}>
                    {formatActivityLevel(profile?.activityLevel)}
                  </Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        {/* Weight Goals Section */}
        <Grid item xs={12} md={6}>
          <Card sx={{ height: '100%' }}>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                <FitnessCenterIcon sx={{ mr: 1, color: 'primary.main' }} />
                <Typography variant="h6">Weight Goals</Typography>
              </Box>
              <Divider sx={{ mb: 2 }} />

              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                  <Typography color="textSecondary">Current Weight</Typography>
                  <Typography fontWeight="medium">
                    {profile?.weight ? `${profile.weight} kg` : '-'}
                  </Typography>
                </Box>

                <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                  <Typography color="textSecondary">Weight Goal</Typography>
                  <Typography fontWeight="medium">
                    {profile?.goal ? `${profile.goal} kg` : '-'}
                  </Typography>
                </Box>

                <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                  <Typography color="textSecondary">Goal Type</Typography>
                  <Chip
                    label={formatGoalType(profile?.goalType)}
                    size="small"
                    color={
                      profile?.goalType === 'LOSE'
                        ? 'error'
                        : profile?.goalType === 'GAIN'
                        ? 'success'
                        : 'default'
                    }
                    variant="outlined"
                  />
                </Box>

                <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                  <Typography color="textSecondary">Weekly Goal</Typography>
                  <Typography fontWeight="medium">
                    {profile?.weeklyGoal ? `${profile.weeklyGoal} kg/week` : '-'}
                  </Typography>
                </Box>

                <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                  <Typography color="textSecondary">Daily Calorie Allowance</Typography>
                  <Typography fontWeight="medium" color="primary">
                    {profile?.allowedDailyIntake ? `${profile.allowedDailyIntake} kcal` : '-'}
                  </Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        {/* BMI Section */}
        <Grid item xs={12} md={6} sx={{ mx: 'auto' }}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                <MonitorWeightIcon sx={{ mr: 1, color: 'primary.main' }} />
                <Typography variant="h6">BMI</Typography>
              </Box>
              <Divider sx={{ mb: 2 }} />

              <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', flexDirection: 'column', py: 2 }}>
                <Typography variant="h2" color="primary" fontWeight="bold">
                  {profile?.bmi ? parseFloat(profile.bmi).toFixed(1) : '-'}
                </Typography>
                <Chip
                  label={bmiInfo.label}
                  color={bmiInfo.color}
                  sx={{ mt: 1 }}
                />
                <Typography variant="body2" color="textSecondary" sx={{ mt: 2, textAlign: 'center' }}>
                  Body Mass Index (BMI) is calculated from your weight and height.
                </Typography>
              </Box>

              <Divider sx={{ my: 2 }} />

              <Box sx={{ display: 'flex', justifyContent: 'space-around' }}>
                <Box sx={{ textAlign: 'center' }}>
                  <Typography variant="caption" color="textSecondary">Underweight</Typography>
                  <Typography variant="body2">&lt; 18.5</Typography>
                </Box>
                <Box sx={{ textAlign: 'center' }}>
                  <Typography variant="caption" color="textSecondary">Normal</Typography>
                  <Typography variant="body2">18.5 - 24.9</Typography>
                </Box>
                <Box sx={{ textAlign: 'center' }}>
                  <Typography variant="caption" color="textSecondary">Overweight</Typography>
                  <Typography variant="body2">25 - 29.9</Typography>
                </Box>
                <Box sx={{ textAlign: 'center' }}>
                  <Typography variant="caption" color="textSecondary">Obese</Typography>
                  <Typography variant="body2">&ge; 30</Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
};

export default Profile;
