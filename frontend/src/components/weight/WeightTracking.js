import React, { useState, useEffect, useCallback } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  CircularProgress,
  Alert,
  Button,
  TextField,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Grid,
  IconButton,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import {
  Chart as ChartJS,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  TimeScale,
} from 'chart.js';
import 'chartjs-adapter-luxon';
import { Line } from 'react-chartjs-2';
import { weightEntryService } from '../../services/api';

ChartJS.register(
  TimeScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend
);

const WeightTracking = () => {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [weightEntries, setWeightEntries] = useState([]);
  const [openDialog, setOpenDialog] = useState(false);
  const [newWeight, setNewWeight] = useState('');
    const [submitting, setSubmitting] = useState(false);

  const fetchWeightEntries = useCallback(async () => {
    try {
      setLoading(true);
      setError('');
      const response = await weightEntryService.getAllWeightEntries();
      setWeightEntries(response.data || []);
    } catch (err) {
      console.error('Failed to fetch weight entries:', err);
      setError('Failed to load weight entries. Please try again.');
      setWeightEntries([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchWeightEntries();
  }, [fetchWeightEntries]);

  const handleAddWeight = async () => {
    if (!newWeight) {
      setError('Please enter a weight.');
      return;
    }

    try {
      setSubmitting(true);
      setError('');
      await weightEntryService.logWeight({
        entryDate: new Date().toISOString().split('T')[0],
        weight: parseFloat(newWeight),
      });
      setOpenDialog(false);
      setNewWeight('');
      fetchWeightEntries();
    } catch (err) {
      console.error('Failed to log weight:', err);
      setError(err.response?.data?.message || 'Failed to log weight. Please try again.');
    } finally {
      setSubmitting(false);
    }
  };

  
  const formatDate = (dateString) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric',
    });
  };

  const formatFullDate = (dateString) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  };

  const getChartData = () => {
    const sortedEntries = [...weightEntries].sort(
      (a, b) => new Date(a.entryDate) - new Date(b.entryDate)
    );

    return {
      datasets: [
        {
          label: 'Weight (kg)',
          data: sortedEntries.map((entry) => ({
            x: entry.entryDate,
            y: parseFloat(entry.weight),
          })),
          borderColor: '#1976d2',
          backgroundColor: 'rgba(25, 118, 210, 0.1)',
          tension: 0.3,
          fill: true,
          pointBackgroundColor: '#1976d2',
          pointBorderColor: '#fff',
          pointBorderWidth: 2,
          pointRadius: 5,
          pointHoverRadius: 7,
        },
      ],
    };
  };

  const chartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        display: false,
      },
      title: {
        display: true,
        text: 'Weight Progress',
        font: {
          size: 16,
          weight: 'bold',
        },
      },
      tooltip: {
        callbacks: {
          label: (context) => `${context.parsed.y} kg`,
        },
      },
    },
    scales: {
      y: {
        beginAtZero: false,
        title: {
          display: true,
          text: 'Weight (kg)',
        },
      },
      x: {
        type: 'time',
        time: {
          unit: 'day',
          displayFormats: {
            day: 'MMM d',
          },
          tooltipFormat: 'MMM d, yyyy',
        },
        title: {
          display: true,
          text: 'Date',
        },
      },
    },
  };

  const getWeightStats = () => {
    if (weightEntries.length === 0) return null;

    const weights = weightEntries.map((e) => parseFloat(e.weight));
    const sortedByDate = [...weightEntries].sort(
      (a, b) => new Date(a.entryDate) - new Date(b.entryDate)
    );

    const firstWeight = parseFloat(sortedByDate[0].weight);
    const latestWeight = parseFloat(sortedByDate[sortedByDate.length - 1].weight);
    const weightChange = latestWeight - firstWeight;

    return {
      current: latestWeight,
      min: Math.min(...weights),
      max: Math.max(...weights),
      change: weightChange,
      entries: weightEntries.length,
    };
  };

  const stats = getWeightStats();

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="50vh">
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4">Weight Tracking</Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => setOpenDialog(true)}
        >
          Log Weight
        </Button>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError('')}>
          {error}
        </Alert>
      )}

      {/* Stats Cards */}
      {stats && (
        <Grid container spacing={3} sx={{ mb: 3 }}>
          <Grid item xs={6} md={3}>
            <Card>
              <CardContent>
                <Typography color="textSecondary" gutterBottom variant="body2">
                  Current Weight
                </Typography>
                <Typography variant="h5" color="primary">
                  {stats.current} kg
                </Typography>
              </CardContent>
            </Card>
          </Grid>
          <Grid item xs={6} md={3}>
            <Card>
              <CardContent>
                <Typography color="textSecondary" gutterBottom variant="body2">
                  Total Change
                </Typography>
                <Typography
                  variant="h5"
                  color={stats.change <= 0 ? 'success.main' : 'error.main'}
                >
                  {stats.change > 0 ? '+' : ''}{stats.change.toFixed(1)} kg
                </Typography>
              </CardContent>
            </Card>
          </Grid>
          <Grid item xs={6} md={3}>
            <Card>
              <CardContent>
                <Typography color="textSecondary" gutterBottom variant="body2">
                  Lowest
                </Typography>
                <Typography variant="h5">
                  {stats.min} kg
                </Typography>
              </CardContent>
            </Card>
          </Grid>
          <Grid item xs={6} md={3}>
            <Card>
              <CardContent>
                <Typography color="textSecondary" gutterBottom variant="body2">
                  Highest
                </Typography>
                <Typography variant="h5">
                  {stats.max} kg
                </Typography>
              </CardContent>
            </Card>
          </Grid>
        </Grid>
      )}

      {/* Chart */}
      <Card sx={{ mb: 3 }}>
        <CardContent>
          {weightEntries.length > 0 ? (
            <Box sx={{ height: 400 }}>
              <Line data={getChartData()} options={chartOptions} />
            </Box>
          ) : (
            <Box sx={{ textAlign: 'center', py: 8 }}>
              <Typography color="textSecondary" gutterBottom>
                No weight entries yet.
              </Typography>
              <Typography color="textSecondary" variant="body2">
                Start tracking your weight by clicking the "Log Weight" button above.
              </Typography>
            </Box>
          )}
        </CardContent>
      </Card>

      {/* Weight History Table */}
      {weightEntries.length > 0 && (
        <Card>
          <CardContent>
            <Typography variant="h6" gutterBottom>
              Weight History
            </Typography>
            <TableContainer component={Paper} variant="outlined">
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>Date</TableCell>
                    <TableCell align="right">Weight (kg)</TableCell>
                                      </TableRow>
                </TableHead>
                <TableBody>
                  {[...weightEntries]
                    .sort((a, b) => new Date(b.entryDate) - new Date(a.entryDate))
                    .map((entry) => (
                      <TableRow key={entry.id}>
                        <TableCell>{formatFullDate(entry.entryDate)}</TableCell>
                        <TableCell align="right">{entry.weight}</TableCell>
                                              </TableRow>
                    ))}
                </TableBody>
              </Table>
            </TableContainer>
          </CardContent>
        </Card>
      )}

      {/* Add Weight Dialog */}
      <Dialog open={openDialog} onClose={() => setOpenDialog(false)} maxWidth="xs" fullWidth>
        <DialogTitle>Log Weight</DialogTitle>
        <DialogContent>
          <Box sx={{ pt: 1 }}>
            <TextField
              label="Weight (kg)"
              type="number"
              value={newWeight}
              onChange={(e) => setNewWeight(e.target.value)}
              fullWidth
              inputProps={{ step: '0.1', min: '20', max: '500' }}
              placeholder="e.g., 70.5"
            />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenDialog(false)} disabled={submitting}>
            Cancel
          </Button>
          <Button
            onClick={handleAddWeight}
            variant="contained"
            disabled={submitting || !newWeight}
          >
            {submitting ? 'Saving...' : 'Save'}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default WeightTracking;
