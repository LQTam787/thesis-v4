import React, { useState, useEffect, useCallback } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  CircularProgress,
  Alert,
  IconButton,
  Divider,
} from '@mui/material';
import ChevronLeftIcon from '@mui/icons-material/ChevronLeft';
import ChevronRightIcon from '@mui/icons-material/ChevronRight';
import TodayIcon from '@mui/icons-material/Today';
import { mealEntryService } from '../../services/api';

const MEAL_TYPE_ORDER = ['BREAKFAST', 'LUNCH', 'DINNER', 'SNACKS', 'OTHER'];

const History = () => {
  const [selectedDate, setSelectedDate] = useState(new Date());
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [mealEntries, setMealEntries] = useState([]);

  const formatDateForApi = (date) => {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  };

  const fetchMealEntries = useCallback(async () => {
    try {
      setLoading(true);
      setError('');
      const dateStr = formatDateForApi(selectedDate);
      const response = await mealEntryService.getMealsByDate(dateStr);
      setMealEntries(response.data || []);
    } catch (err) {
      console.error('Failed to fetch meal entries:', err);
      setError('Failed to load meal entries. Please try again.');
      setMealEntries([]);
    } finally {
      setLoading(false);
    }
  }, [selectedDate]);

  useEffect(() => {
    fetchMealEntries();
  }, [fetchMealEntries]);

  const handlePreviousDay = () => {
    setSelectedDate((prev) => {
      const newDate = new Date(prev);
      newDate.setDate(newDate.getDate() - 1);
      return newDate;
    });
  };

  const handleNextDay = () => {
    setSelectedDate((prev) => {
      const newDate = new Date(prev);
      newDate.setDate(newDate.getDate() + 1);
      return newDate;
    });
  };

  const handleToday = () => {
    setSelectedDate(new Date());
  };

  const isSameDay = (date1, date2) => {
    return (
      date1.getDate() === date2.getDate() &&
      date1.getMonth() === date2.getMonth() &&
      date1.getFullYear() === date2.getFullYear()
    );
  };

  const isToday = () => isSameDay(selectedDate, new Date());

  const isYesterday = () => {
    const yesterday = new Date();
    yesterday.setDate(yesterday.getDate() - 1);
    return isSameDay(selectedDate, yesterday);
  };

  const isTomorrow = () => {
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    return isSameDay(selectedDate, tomorrow);
  };

  const getDateDisplayText = () => {
    if (isToday()) return 'Today';
    if (isYesterday()) return 'Yesterday';
    if (isTomorrow()) return 'Tomorrow';
    return selectedDate.toLocaleDateString('en-US', {
      weekday: 'long',
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  };

  const groupMealsByType = (meals) => {
    const grouped = {};
    meals.forEach((meal) => {
      const type = meal.mealType || 'OTHER';
      if (!grouped[type]) {
        grouped[type] = [];
      }
      grouped[type].push(meal);
    });

    // Sort by meal type order
    const sortedGrouped = {};
    MEAL_TYPE_ORDER.forEach((type) => {
      if (grouped[type]) {
        sortedGrouped[type] = grouped[type];
      }
    });

    return sortedGrouped;
  };

  const getTotalCalories = () => {
    return mealEntries.reduce((sum, meal) => sum + (meal.calories || 0), 0);
  };

  const formatMealType = (type) => {
    return type.charAt(0) + type.slice(1).toLowerCase();
  };

  const groupedMeals = groupMealsByType(mealEntries);

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Meal History
      </Typography>

      {/* Date Navigation */}
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Box
            sx={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
            }}
          >
            <Box sx={{ width: 40 }} />

            <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
              <IconButton onClick={handlePreviousDay} aria-label="previous day">
                <ChevronLeftIcon />
              </IconButton>

              <Box sx={{ textAlign: 'center', width: 300 }}>
                <Typography variant="h6">
                  {getDateDisplayText()}
                </Typography>
              </Box>

              <IconButton onClick={handleNextDay} aria-label="next day">
                <ChevronRightIcon />
              </IconButton>
            </Box>

            <Box sx={{ width: 40 }}>
              {!isToday() && (
                <IconButton
                  onClick={handleToday}
                  aria-label="go to today"
                  color="primary"
                >
                  <TodayIcon />
                </IconButton>
              )}
            </Box>
          </Box>
        </CardContent>
      </Card>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      {loading ? (
        <Box display="flex" justifyContent="center" alignItems="center" minHeight="30vh">
          <CircularProgress />
        </Box>
      ) : (
        <>
          {/* Summary Card */}
          <Card sx={{ mb: 3 }}>
            <CardContent>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <Typography variant="h6">Daily Summary</Typography>
                <Box sx={{ textAlign: 'right' }}>
                  <Typography variant="h5" color="primary">
                    {getTotalCalories()} cal
                  </Typography>
                  <Typography variant="caption" color="textSecondary">
                    {mealEntries.length} meal{mealEntries.length !== 1 ? 's' : ''} logged
                  </Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>

          {/* Meals by Type */}
          {mealEntries.length > 0 ? (
            <Card>
              <CardContent>
                {Object.entries(groupedMeals).map(([mealType, meals], index) => (
                  <Box key={mealType}>
                    {index > 0 && <Divider sx={{ my: 2 }} />}
                    <Typography
                      variant="subtitle1"
                      color="primary"
                      sx={{ fontWeight: 'bold', mb: 1 }}
                    >
                      {formatMealType(mealType)}
                    </Typography>
                    {meals.map((meal) => (
                      <Box
                        key={meal.id}
                        sx={{
                          display: 'flex',
                          justifyContent: 'space-between',
                          alignItems: 'center',
                          py: 1,
                          px: 2,
                          bgcolor: 'grey.50',
                          borderRadius: 1,
                          mb: 1,
                        }}
                      >
                        <Box>
                          <Typography variant="body1">{meal.foodName}</Typography>
                          <Typography variant="caption" color="textSecondary">
                            {meal.entryTime?.slice(0, 5)}
                          </Typography>
                        </Box>
                        <Typography variant="body2" color="primary" sx={{ fontWeight: 'bold' }}>
                          {meal.calories} cal
                        </Typography>
                      </Box>
                    ))}
                    <Typography
                      variant="body2"
                      color="textSecondary"
                      sx={{ textAlign: 'right', mt: 1 }}
                    >
                      Subtotal: {meals.reduce((sum, m) => sum + (m.calories || 0), 0)} cal
                    </Typography>
                  </Box>
                ))}
              </CardContent>
            </Card>
          ) : (
            <Card>
              <CardContent>
                <Typography color="textSecondary" sx={{ textAlign: 'center', py: 4 }}>
                  No meals logged for this day.
                </Typography>
              </CardContent>
            </Card>
          )}
        </>
      )}
    </Box>
  );
};

export default History;
