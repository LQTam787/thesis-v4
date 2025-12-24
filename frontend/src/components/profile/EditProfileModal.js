import React, { useState, useEffect } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Grid,
  Alert,
  CircularProgress,
  Box,
} from '@mui/material';
import { userService } from '../../services/api';

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

const EditProfileModal = ({ open, onClose, profile, onSuccess }) => {
  const [formData, setFormData] = useState({
    name: '',
    sex: '',
    dob: '',
    height: '',
    weight: '',
    activityLevel: '',
    goal: '',
    weeklyGoal: '',
  });
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (open && profile) {
      setFormData({
        name: profile.name || '',
        sex: profile.sex || '',
        dob: profile.dob || '',
        height: profile.height || '',
        weight: profile.weight || '',
        activityLevel: profile.activityLevel || '',
        goal: profile.goal || '',
        weeklyGoal: profile.weeklyGoal || '',
      });
      setError('');
    }
  }, [open, profile]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (!formData.name.trim()) {
      setError('Name is required');
      return;
    }

    if (!formData.dob) {
      setError('Date of birth is required');
      return;
    }

    const height = parseFloat(formData.height);
    if (isNaN(height) || height < 50 || height > 300) {
      setError('Height must be between 50 and 300 cm');
      return;
    }

    const weight = parseFloat(formData.weight);
    if (isNaN(weight) || weight < 20 || weight > 500) {
      setError('Weight must be between 20 and 500 kg');
      return;
    }

    const goal = parseFloat(formData.goal);
    if (isNaN(goal) || goal < 20 || goal > 500) {
      setError('Goal weight must be between 20 and 500 kg');
      return;
    }

    const weeklyGoal = parseFloat(formData.weeklyGoal);
    if (isNaN(weeklyGoal) || weeklyGoal < 0.1 || weeklyGoal > 1.0) {
      setError('Weekly goal must be between 0.1 and 1.0 kg');
      return;
    }

    try {
      setSubmitting(true);
      const updateData = {
        name: formData.name.trim(),
        sex: formData.sex,
        dob: formData.dob,
        height: height,
        weight: weight,
        activityLevel: formData.activityLevel,
        goal: goal,
        weeklyGoal: weeklyGoal,
      };

      const response = await userService.updateProfile(updateData);
      onSuccess?.(response.data);
      onClose();
    } catch (err) {
      console.error('Failed to update profile:', err);
      setError(err.response?.data?.message || 'Failed to update profile. Please try again.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>Edit Profile</DialogTitle>
      <form onSubmit={handleSubmit}>
        <DialogContent>
          {error && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {error}
            </Alert>
          )}

          <Grid container spacing={2}>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Name"
                name="name"
                value={formData.name}
                onChange={handleChange}
                required
                disabled={submitting}
              />
            </Grid>

            <Grid item xs={12} sm={6}>
              <FormControl fullWidth required disabled={submitting}>
                <InputLabel>Sex</InputLabel>
                <Select
                  name="sex"
                  value={formData.sex}
                  onChange={handleChange}
                  label="Sex"
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
                fullWidth
                label="Date of Birth"
                name="dob"
                type="date"
                value={formData.dob}
                onChange={handleChange}
                required
                disabled={submitting}
                InputLabelProps={{ shrink: true }}
              />
            </Grid>

            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="Height (cm)"
                name="height"
                type="number"
                value={formData.height}
                onChange={handleChange}
                required
                disabled={submitting}
                inputProps={{ min: 50, max: 300, step: 0.1 }}
              />
            </Grid>

            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="Weight (kg)"
                name="weight"
                type="number"
                value={formData.weight}
                onChange={handleChange}
                required
                disabled={submitting}
                inputProps={{ min: 20, max: 500, step: 0.1 }}
              />
            </Grid>

            <Grid item xs={12}>
              <FormControl fullWidth required disabled={submitting}>
                <InputLabel>Activity Level</InputLabel>
                <Select
                  name="activityLevel"
                  value={formData.activityLevel}
                  onChange={handleChange}
                  label="Activity Level"
                >
                  {activityLevels.map((option) => (
                    <MenuItem key={option.value} value={option.value}>
                      {option.label}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>

            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="Goal Weight (kg)"
                name="goal"
                type="number"
                value={formData.goal}
                onChange={handleChange}
                required
                disabled={submitting}
                inputProps={{ min: 20, max: 500, step: 0.1 }}
                helperText="Your target weight"
              />
            </Grid>

            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="Weekly Goal (kg/week)"
                name="weeklyGoal"
                type="number"
                value={formData.weeklyGoal}
                onChange={handleChange}
                required
                disabled={submitting}
                inputProps={{ min: 0.1, max: 1.0, step: 0.1 }}
                helperText="Weight change per week (0.1-1.0)"
              />
            </Grid>
          </Grid>
          
        </DialogContent>

        <DialogActions sx={{ px: 3, pb: 2 }}>
          <Button onClick={onClose} disabled={submitting}>
            Cancel
          </Button>
          <Button
            type="submit"
            variant="contained"
            disabled={submitting}
            startIcon={submitting ? <CircularProgress size={20} /> : null}
          >
            {submitting ? 'Saving...' : 'Save Changes'}
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  );
};

export default EditProfileModal;
