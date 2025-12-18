import React, { useState, useEffect } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Grid,
  CircularProgress,
  Alert,
  Chip,
  TextField,
  InputAdornment,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Avatar,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
} from '@mui/material';
import {
  Search as SearchIcon,
  Restaurant as RestaurantIcon,
  LocalFireDepartment as CaloriesIcon,
  Add as AddIcon,
} from '@mui/icons-material';
import { foodService } from '../../services/api';

const MEAL_TYPES = ['ALL', 'BREAKFAST', 'LUNCH', 'SNACKS', 'DINNER', 'OTHER'];

const getMealTypeColor = (mealType) => {
  const colors = {
    BREAKFAST: '#FF9800',
    LUNCH: '#4CAF50',
    SNACKS: '#9C27B0',
    DINNER: '#2196F3',
    OTHER: '#607D8B',
  };
  return colors[mealType] || '#607D8B';
};

const FORM_MEAL_TYPES = ['BREAKFAST', 'LUNCH', 'SNACKS', 'DINNER', 'OTHER'];

const Foods = () => {
  const [foods, setFoods] = useState([]);
  const [filteredFoods, setFilteredFoods] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [searchTerm, setSearchTerm] = useState('');
  const [mealTypeFilter, setMealTypeFilter] = useState('ALL');
  const [openDialog, setOpenDialog] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [formData, setFormData] = useState({
    name: '',
    mealType: '',
    calories: '',
  });
  const [formError, setFormError] = useState('');

  useEffect(() => {
    fetchFoods();
  }, []);

  useEffect(() => {
    filterFoods();
  }, [foods, searchTerm, mealTypeFilter]);

  const fetchFoods = async () => {
    try {
      setLoading(true);
      setError('');
      const response = await foodService.getAllFoods();
      setFoods(response.data);
    } catch (err) {
      console.error('Failed to fetch foods:', err);
      setError('Failed to load foods. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const filterFoods = () => {
    let result = [...foods];

    if (searchTerm) {
      result = result.filter((food) =>
        food.name.toLowerCase().includes(searchTerm.toLowerCase())
      );
    }

    if (mealTypeFilter !== 'ALL') {
      result = result.filter((food) => food.mealType === mealTypeFilter);
    }

    setFilteredFoods(result);
  };

  const handleOpenDialog = () => {
    setOpenDialog(true);
    setFormError('');
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
    setFormData({ name: '', mealType: '', calories: '' });
    setFormError('');
  };

  const handleFormChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async () => {
    if (!formData.name.trim()) {
      setFormError('Food name is required');
      return;
    }
    if (!formData.mealType) {
      setFormError('Meal type is required');
      return;
    }
    if (!formData.calories || formData.calories < 0) {
      setFormError('Valid calories value is required');
      return;
    }

    try {
      setSubmitting(true);
      setFormError('');
      await foodService.addFood({
        name: formData.name.trim(),
        mealType: formData.mealType,
        calories: parseInt(formData.calories, 10),
        image: null,
      });
      handleCloseDialog();
      fetchFoods();
    } catch (err) {
      console.error('Failed to add food:', err);
      setFormError(err.response?.data?.message || 'Failed to add food. Please try again.');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="50vh">
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Foods
      </Typography>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
        <Typography variant="subtitle1" color="textSecondary">
          Browse available foods for meal tracking
        </Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={handleOpenDialog}
        >
          Add Food
        </Button>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      <Box sx={{ display: 'flex', gap: 2, mb: 3, flexWrap: 'wrap' }}>
        <TextField
          placeholder="Search foods..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          sx={{ flexGrow: 1, minWidth: 200 }}
          InputProps={{
            startAdornment: (
              <InputAdornment position="start">
                <SearchIcon />
              </InputAdornment>
            ),
          }}
        />
        <FormControl sx={{ minWidth: 150 }}>
          <InputLabel>Meal Type</InputLabel>
          <Select
            value={mealTypeFilter}
            label="Meal Type"
            onChange={(e) => setMealTypeFilter(e.target.value)}
          >
            {MEAL_TYPES.map((type) => (
              <MenuItem key={type} value={type}>
                {type === 'ALL' ? 'All Types' : type.charAt(0) + type.slice(1).toLowerCase()}
              </MenuItem>
            ))}
          </Select>
        </FormControl>
      </Box>

      <Typography variant="body2" color="textSecondary" sx={{ mb: 2 }}>
        Showing {filteredFoods.length} of {foods.length} foods
      </Typography>

      {filteredFoods.length === 0 ? (
        <Card>
          <CardContent>
            <Box display="flex" flexDirection="column" alignItems="center" py={4}>
              <RestaurantIcon sx={{ fontSize: 64, color: 'text.secondary', mb: 2 }} />
              <Typography variant="h6" color="textSecondary">
                No foods found
              </Typography>
              <Typography variant="body2" color="textSecondary">
                {searchTerm || mealTypeFilter !== 'ALL'
                  ? 'Try adjusting your search or filter'
                  : 'No foods available yet'}
              </Typography>
            </Box>
          </CardContent>
        </Card>
      ) : (
        <Grid container spacing={2}>
          {filteredFoods.map((food) => (
            <Grid item xs={12} sm={6} md={4} lg={3} key={food.id}>
              <Card
                sx={{
                  height: '100%',
                  display: 'flex',
                  flexDirection: 'column',
                  transition: 'transform 0.2s, box-shadow 0.2s',
                  '&:hover': {
                    transform: 'translateY(-4px)',
                    boxShadow: 4,
                  },
                }}
              >
                <CardContent sx={{ flexGrow: 1 }}>
                  <Box display="flex" alignItems="center" mb={2}>
                    <Avatar
                      src={food.image}
                      sx={{
                        width: 48,
                        height: 48,
                        bgcolor: getMealTypeColor(food.mealType),
                        mr: 2,
                      }}
                    >
                      <RestaurantIcon />
                    </Avatar>
                    <Box sx={{ flexGrow: 1, minWidth: 0 }}>
                      <Typography
                        variant="h6"
                        component="div"
                        sx={{
                          overflow: 'hidden',
                          textOverflow: 'ellipsis',
                          whiteSpace: 'nowrap',
                        }}
                      >
                        {food.name}
                      </Typography>
                      <Chip
                        label={food.mealType}
                        size="small"
                        sx={{
                          bgcolor: getMealTypeColor(food.mealType),
                          color: 'white',
                          fontSize: '0.7rem',
                        }}
                      />
                    </Box>
                  </Box>

                  <Box display="flex" alignItems="center" justifyContent="space-between">
                    <Box display="flex" alignItems="center">
                      <CaloriesIcon sx={{ color: 'warning.main', mr: 0.5, fontSize: 20 }} />
                      <Typography variant="body1" fontWeight="bold">
                        {food.calories}
                      </Typography>
                      <Typography variant="body2" color="textSecondary" sx={{ ml: 0.5 }}>
                        cal
                      </Typography>
                    </Box>
                  </Box>
                </CardContent>
              </Card>
            </Grid>
          ))}
        </Grid>
      )}

      <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="sm" fullWidth>
        <DialogTitle>Add New Food</DialogTitle>
        <DialogContent>
          {formError && (
            <Alert severity="error" sx={{ mb: 2, mt: 1 }}>
              {formError}
            </Alert>
          )}
          <TextField
            autoFocus
            margin="dense"
            name="name"
            label="Food Name"
            type="text"
            fullWidth
            value={formData.name}
            onChange={handleFormChange}
            sx={{ mb: 2 }}
          />
          <FormControl fullWidth sx={{ mb: 2 }}>
            <InputLabel>Meal Type</InputLabel>
            <Select
              name="mealType"
              value={formData.mealType}
              label="Meal Type"
              onChange={handleFormChange}
            >
              {FORM_MEAL_TYPES.map((type) => (
                <MenuItem key={type} value={type}>
                  {type.charAt(0) + type.slice(1).toLowerCase()}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
          <TextField
            margin="dense"
            name="calories"
            label="Calories"
            type="number"
            fullWidth
            value={formData.calories}
            onChange={handleFormChange}
            inputProps={{ min: 0, max: 10000 }}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDialog} disabled={submitting}>
            Cancel
          </Button>
          <Button onClick={handleSubmit} variant="contained" disabled={submitting}>
            {submitting ? 'Adding...' : 'Add Food'}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default Foods;
