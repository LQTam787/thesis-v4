import React, { useState, useEffect, useCallback } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  CircularProgress,
  Alert,
  Button,
  Paper,
  Divider,
} from '@mui/material';
import RefreshIcon from '@mui/icons-material/Refresh';
import RestaurantMenuIcon from '@mui/icons-material/RestaurantMenu';
import RateReviewIcon from '@mui/icons-material/RateReview';
import { planService, reviewService } from '../../services/api';

const Plan = () => {
  const [loading, setLoading] = useState(true);
  const [generating, setGenerating] = useState(false);
  const [generatingReview, setGeneratingReview] = useState(false);
  const [error, setError] = useState('');
  const [planData, setPlanData] = useState({ text: null, createdAt: null });
  const [reviewData, setReviewData] = useState({ text: null, createdAt: null });

  const isOlderThanOneWeek = (dateString) => {
    if (!dateString) return true;
    const createdDate = new Date(dateString);
    const now = new Date();
    const diffTime = now - createdDate;
    const diffDays = diffTime / (1000 * 60 * 60 * 24);
    return diffDays >= 7;
  };

  const fetchReview = useCallback(async () => {
    try {
      const response = await reviewService.getReview();
      setReviewData(response.data);
      return response.data;
    } catch (err) {
      console.error('Failed to fetch review:', err);
      return { text: null, createdAt: null };
    }
  }, []);

  const generateNewReview = async () => {
    try {
      setGeneratingReview(true);
      const response = await reviewService.generateReview();
      setReviewData(response.data);
      return response.data;
    } catch (err) {
      console.error('Failed to generate review:', err);
      setError('Failed to generate your review. Please try again.');
      return null;
    } finally {
      setGeneratingReview(false);
    }
  };

  const fetchPlan = useCallback(async () => {
    try {
      setLoading(true);
      setError('');
      
      const [planResponse, reviewResult] = await Promise.all([
        planService.getPlan(),
        fetchReview()
      ]);
      const data = planResponse.data;

      if (!data.text || isOlderThanOneWeek(data.createdAt)) {
        if (data.text) {
          await generateNewReview();
        }
        await generateNewPlan();
      } else {
        setPlanData(data);
        setLoading(false);
      }
    } catch (err) {
      console.error('Failed to fetch plan:', err);
      setError('Failed to load your meal plan. Please try again.');
      setLoading(false);
    }
  }, [fetchReview]);

  const generateNewPlan = async () => {
    try {
      setGenerating(true);
      setError('');
      const response = await planService.generatePlan();
      setPlanData(response.data);
    } catch (err) {
      console.error('Failed to generate plan:', err);
      setError('Failed to generate your meal plan. Please try again.');
    } finally {
      setGenerating(false);
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPlan();
  }, [fetchPlan]);

  const formatDate = (dateString) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      weekday: 'long',
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  if (loading || generating || generatingReview) {
    return (
      <Box display="flex" flexDirection="column" justifyContent="center" alignItems="center" minHeight="50vh" gap={2}>
        <CircularProgress />
        <Typography color="textSecondary">
          {generatingReview 
            ? 'Generating your weekly review...' 
            : generating 
              ? 'Generating your personalized meal plan...' 
              : 'Loading your meal plan...'}
        </Typography>
      </Box>
    );
  }

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 2 }}>
        <Box>
          <Typography variant="h4" gutterBottom>
            Meal Plan
          </Typography>
          <Typography variant="subtitle1" color="textSecondary" gutterBottom>
            Your personalized 7-day meal plan
          </Typography>
        </Box>
        {/*<Button*/}
        {/*  variant="contained"*/}
        {/*  startIcon={<RefreshIcon />}*/}
        {/*  onClick={generateNewPlan}*/}
        {/*  disabled={generating}*/}
        {/*>*/}
        {/*  Generate New Plan*/}
        {/*</Button>*/}
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      {planData.createdAt && (
        <Typography variant="body2" color="textSecondary" sx={{ mb: 2 }}>
          Generated on: {formatDate(planData.createdAt)}
        </Typography>
      )}

      <Card>
        <CardContent>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
            <RestaurantMenuIcon color="primary" />
            <Typography variant="h6">This Week's Plan</Typography>
          </Box>
          
          {planData.text ? (
            <Paper 
              elevation={0} 
              sx={{ 
                p: 3, 
                bgcolor: 'grey.50', 
                borderRadius: 2,
                whiteSpace: 'pre-wrap',
                fontFamily: 'inherit',
                lineHeight: 1.8,
              }}
            >
              <Typography variant="body1" component="div">
                {planData.text}
              </Typography>
            </Paper>
          ) : (
            <Typography color="textSecondary">
              No meal plan available. Click "Generate New Plan" to create one.
            </Typography>
          )}
        </CardContent>
      </Card>

      {reviewData.text && (
          <Card sx={{ mb: 3 }}>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
                <RateReviewIcon color="secondary" />
                <Typography variant="h6">Last Week's Review</Typography>
              </Box>

              {reviewData.createdAt && (
                  <Typography variant="body2" color="textSecondary" sx={{ mb: 2 }}>
                    Generated on: {formatDate(reviewData.createdAt)}
                  </Typography>
              )}

              <Paper
                  elevation={0}
                  sx={{
                    p: 3,
                    bgcolor: 'secondary.50',
                    borderRadius: 2,
                    whiteSpace: 'pre-wrap',
                    fontFamily: 'inherit',
                    lineHeight: 1.8,
                    borderLeft: 4,
                    borderColor: 'secondary.main',
                  }}
              >
                <Typography variant="body1" component="div">
                  {reviewData.text}
                </Typography>
              </Paper>
            </CardContent>
          </Card>
      )}
    </Box>
  );
};

export default Plan;
