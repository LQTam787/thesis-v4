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
  Box,
  Typography,
  CircularProgress,
  Alert,
  Autocomplete,
  Chip,
} from '@mui/material';
import { foodService, mealEntryService } from '../../services/api';

const MEAL_TYPES = ['BREAKFAST', 'LUNCH', 'DINNER', 'SNACKS', 'OTHER'];

const LogMealModal = ({ open, onClose, onSuccess }) => {
  const [foods, setFoods] = useState([]);
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [selectedFood, setSelectedFood] = useState(null);
  const [mealTypeFilter, setMealTypeFilter] = useState('');

  useEffect(() => {
    if (open) {
      fetchFoods();
      setSelectedFood(null);
      setError('');
    }
  }, [open]);

  const fetchFoods = async () => {
    try {
      setLoading(true);
      const response = await foodService.getAllFoods();
      setFoods(response.data);
    } catch (err) {
      console.error('Failed to fetch foods:', err);
      setError('Failed to load foods. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const filteredFoods = mealTypeFilter
    ? foods.filter((food) => food.mealType === mealTypeFilter)
    : foods;

  const handleSubmit = async () => {
    if (!selectedFood) {
      setError('Please select a food item');
      return;
    }

    try {
      setSubmitting(true);
      setError('');

      const now = new Date();
      const mealData = {
        foodId: selectedFood.id,
        entryDate: now.toISOString().split('T')[0],
        entryTime: now.toTimeString().split(' ')[0],
      };

      await mealEntryService.logMeal(mealData);
      onSuccess?.();
      onClose();
    } catch (err) {
      console.error('Failed to log meal:', err);
      setError(err.response?.data?.message || 'Failed to log meal. Please try again.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleClose = () => {
    if (!submitting) {
      setSelectedFood(null);
      setError('');
      setMealTypeFilter('');
      onClose();
    }
  };

  return (
    <Dialog open={open} onClose={handleClose} maxWidth="sm" fullWidth>
      <DialogTitle>Log Meal</DialogTitle>
      <DialogContent>
        {error && (
          <Alert severity="error" sx={{ mb: 2, mt: 1 }}>
            {error}
          </Alert>
        )}

        <Box sx={{ mt: 2, display: 'flex', flexDirection: 'column', gap: 2 }}>
          <FormControl fullWidth size="small">
            <InputLabel>Filter by Meal Type</InputLabel>
            <Select
              value={mealTypeFilter}
              label="Filter by Meal Type"
              onChange={(e) => setMealTypeFilter(e.target.value)}
            >
              <MenuItem value="">All Types</MenuItem>
              {MEAL_TYPES.map((type) => (
                <MenuItem key={type} value={type}>
                  {type.charAt(0) + type.slice(1).toLowerCase()}
                </MenuItem>
              ))}
            </Select>
          </FormControl>

          {loading ? (
            <Box display="flex" justifyContent="center" py={3}>
              <CircularProgress size={30} />
            </Box>
          ) : (
            <Autocomplete
              options={filteredFoods}
              getOptionLabel={(option) => option.name}
              value={selectedFood}
              onChange={(event, newValue) => setSelectedFood(newValue)}
              renderInput={(params) => (
                <TextField
                  {...params}
                  label="Select Food"
                  placeholder="Search for a food..."
                  required
                />
              )}
              renderOption={(props, option) => (
                <Box component="li" {...props}>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', width: '100%', alignItems: 'center' }}>
                    <Box>
                      <Typography variant="body1">{option.name}</Typography>
                      <Typography variant="caption" color="textSecondary">
                        {option.mealType.charAt(0) + option.mealType.slice(1).toLowerCase()}
                      </Typography>
                    </Box>
                    <Chip
                      label={`${option.calories} cal`}
                      size="small"
                      color="primary"
                      variant="outlined"
                    />
                  </Box>
                </Box>
              )}
              isOptionEqualToValue={(option, value) => option.id === value.id}
              noOptionsText="No foods found"
            />
          )}

          {selectedFood && (
            <Box sx={{ p: 2, bgcolor: 'grey.100', borderRadius: 1 }}>
              <Typography variant="subtitle2" color="textSecondary">
                Selected Food
              </Typography>
              <Typography variant="h6">{selectedFood.name}</Typography>
              <Box sx={{ display: 'flex', gap: 2, mt: 1 }}>
                <Chip
                  label={selectedFood.mealType.charAt(0) + selectedFood.mealType.slice(1).toLowerCase()}
                  size="small"
                />
                <Chip
                  label={`${selectedFood.calories} calories`}
                  size="small"
                  color="primary"
                />
              </Box>
            </Box>
          )}
        </Box>
      </DialogContent>
      <DialogActions sx={{ px: 3, pb: 2 }}>
        <Button onClick={handleClose} disabled={submitting}>
          Cancel
        </Button>
        <Button
          onClick={handleSubmit}
          variant="contained"
          disabled={submitting || !selectedFood}
        >
          {submitting ? <CircularProgress size={24} /> : 'Log Meal'}
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default LogMealModal;
