import React, { useState } from 'react';
import { Link as RouterLink, useNavigate } from 'react-router-dom';
import {
  Container,
  Box,
  Typography,
  TextField,
  Button,
  Link,
  Paper,
  Alert,
  Grid,
  MenuItem,
  FormControl,
  InputLabel,
  Select,
} from '@mui/material';
import { useAuth } from '../../context/AuthContext';

const activityLevels = [
  { value: 'SEDENTARY', label: 'Sedentary (little to no exercise)' },
  { value: 'LIGHTLY_ACTIVE', label: 'Lightly Active (1-3 days/week)' },
  { value: 'MODERATELY_ACTIVE', label: 'Moderately Active (3-5 days/week)' },
  { value: 'VERY_ACTIVE', label: 'Very Active (6-7 days/week)' },
];

const sexOptions = [
  { value: 'MALE', label: 'Male' },
  { value: 'FEMALE', label: 'Female' },
];

// Helper function to derive goalType from weight and goal
const deriveGoalType = (currentWeight, targetWeight) => {
  const weight = parseFloat(currentWeight);
  const goal = parseFloat(targetWeight);
  
  if (isNaN(weight) || isNaN(goal)) return 'MAINTAIN';
  
  if (goal < weight) return 'LOSE';
  if (goal > weight) return 'GAIN';
  return 'MAINTAIN';
};

const Register = () => {
  const navigate = useNavigate();
  const { register } = useAuth();
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    password: '',
    confirmPassword: '',
    dob: '',
    sex: 'MALE',
    weight: '',
    height: '',
    activityLevel: 'MODERATELY_ACTIVE',
    goal: '',
    weeklyGoal: '0.5',
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (formData.password !== formData.confirmPassword) {
      setError('Passwords do not match');
      return;
    }

    if (formData.password.length < 6) {
      setError('Password must be at least 6 characters');
      return;
    }

    setLoading(true);

    const weight = parseFloat(formData.weight);
    const goal = formData.goal ? parseFloat(formData.goal) : weight;
    const goalType = deriveGoalType(weight, goal);

    const userData = {
      name: formData.name,
      email: formData.email,
      password: formData.password,
      dob: formData.dob,
      sex: formData.sex,
      weight: weight,
      height: parseFloat(formData.height),
      activityLevel: formData.activityLevel,
      goal: goal,
      goalType: goalType,
      weeklyGoal: parseFloat(formData.weeklyGoal),
    };

    const result = await register(userData);
    
    if (result.success) {
      navigate('/dashboard');
    } else {
      setError(result.message);
    }
    setLoading(false);
  };

  return (
    <Container component="main" maxWidth="sm">
      <Box
        sx={{
          marginTop: 4,
          marginBottom: 4,
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
        }}
      >
        <Paper elevation={3} sx={{ p: 4, width: '100%' }}>
          <Typography component="h1" variant="h4" align="center" gutterBottom>
            Calorie Tracker
          </Typography>
          <Typography component="h2" variant="h6" align="center" color="textSecondary" gutterBottom>
            Create Account
          </Typography>
          
          {error && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {error}
            </Alert>
          )}

          <Box component="form" onSubmit={handleSubmit} sx={{ mt: 1 }}>
            <Grid container spacing={2}>
              <Grid item xs={12}>
                <TextField
                  required
                  fullWidth
                  id="name"
                  label="Full Name"
                  name="name"
                  autoComplete="name"
                  value={formData.name}
                  onChange={handleChange}
                />
              </Grid>
              <Grid item xs={12}>
                <TextField
                  required
                  fullWidth
                  id="email"
                  label="Email Address"
                  name="email"
                  autoComplete="email"
                  type="email"
                  value={formData.email}
                  onChange={handleChange}
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  required
                  fullWidth
                  name="password"
                  label="Password"
                  type="password"
                  id="password"
                  value={formData.password}
                  onChange={handleChange}
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  required
                  fullWidth
                  name="confirmPassword"
                  label="Confirm Password"
                  type="password"
                  id="confirmPassword"
                  value={formData.confirmPassword}
                  onChange={handleChange}
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  required
                  fullWidth
                  id="dob"
                  label="Date of Birth"
                  name="dob"
                  type="date"
                  InputLabelProps={{ shrink: true }}
                  value={formData.dob}
                  onChange={handleChange}
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <FormControl fullWidth required>
                  <InputLabel id="sex-label">Sex</InputLabel>
                  <Select
                    labelId="sex-label"
                    id="sex"
                    name="sex"
                    value={formData.sex}
                    label="Sex"
                    onChange={handleChange}
                  >
                    {sexOptions.map((option) => (
                      <MenuItem key={option.value} value={option.value}>
                        {option.label}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  required
                  fullWidth
                  id="weight"
                  label="Current Weight (kg)"
                  name="weight"
                  type="number"
                  inputProps={{ step: '0.1', min: '20', max: '300' }}
                  value={formData.weight}
                  onChange={handleChange}
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  required
                  fullWidth
                  id="height"
                  label="Height (cm)"
                  name="height"
                  type="number"
                  inputProps={{ step: '0.1', min: '100', max: '250' }}
                  value={formData.height}
                  onChange={handleChange}
                />
              </Grid>
              <Grid item xs={12}>
                <FormControl fullWidth required>
                  <InputLabel id="activityLevel-label">Activity Level</InputLabel>
                  <Select
                    labelId="activityLevel-label"
                    id="activityLevel"
                    name="activityLevel"
                    value={formData.activityLevel}
                    label="Activity Level"
                    onChange={handleChange}
                  >
                    {activityLevels.map((level) => (
                      <MenuItem key={level.value} value={level.value}>
                        {level.label}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
              </Grid>
              <Grid item xs={12}>
                <TextField
                  fullWidth
                  id="goal"
                  label="Target Weight (kg)"
                  name="goal"
                  type="number"
                  inputProps={{ step: '0.1', min: '20', max: '300' }}
                  value={formData.goal}
                  onChange={handleChange}
                  helperText="Leave empty to maintain current weight"
                />
              </Grid>
              <Grid item xs={12}>
                <TextField
                  required
                  fullWidth
                  id="weeklyGoal"
                  label="Weekly Goal (kg/week)"
                  name="weeklyGoal"
                  type="number"
                  inputProps={{ step: '0.1', min: '0.1', max: '1.0' }}
                  value={formData.weeklyGoal}
                  onChange={handleChange}
                  helperText="Don't worry about this if you aim to maintain your weight."
                />
              </Grid>
            </Grid>
            <Button
              type="submit"
              fullWidth
              variant="contained"
              sx={{ mt: 3, mb: 2 }}
              disabled={loading}
            >
              {loading ? 'Creating Account...' : 'Sign Up'}
            </Button>
            <Box sx={{ textAlign: 'center' }}>
              <Link component={RouterLink} to="/login" variant="body2">
                Already have an account? Sign In
              </Link>
            </Box>
          </Box>
        </Paper>
      </Box>
    </Container>
  );
};

export default Register;
