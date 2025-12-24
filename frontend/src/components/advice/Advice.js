import React, { useState, useRef, useEffect } from 'react';
import {
  Box,
  Paper,
  TextField,
  IconButton,
  Typography,
  CircularProgress,
  Alert,
  Avatar,
  List,
  ListItem,
} from '@mui/material';
import SendIcon from '@mui/icons-material/Send';
import SmartToyIcon from '@mui/icons-material/SmartToy';
import PersonIcon from '@mui/icons-material/Person';
import { adviceService } from '../../services/api';

const Advice = () => {
  const [messages, setMessages] = useState([
    {
      role: 'assistant',
      content: "Hello! I'm your AI diet advisor. I can help you with nutrition advice, meal suggestions, and answer questions about healthy eating habits. How can I assist you today?",
    },
  ]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const messagesEndRef = useRef(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const handleSend = async () => {
    if (!input.trim() || loading) return;

    const userMessage = input.trim();
    setInput('');
    setError('');

    setMessages((prev) => [...prev, { role: 'user', content: userMessage }]);
    setLoading(true);

    try {
      const response = await adviceService.chat(userMessage);
      const data = response.data;

      if (data.success) {
        setMessages((prev) => [
          ...prev,
          { role: 'assistant', content: data.response },
        ]);
      } else {
        setError(data.error || 'Failed to get response from AI advisor');
      }
    } catch (err) {
      setError(
        err.response?.data?.error ||
          'Failed to connect to AI advisor. Please try again.'
      );
    } finally {
      setLoading(false);
    }
  };

  const handleKeyPress = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  const formatMessage = (content) => {
    return content.split('\n').map((line, index) => (
      <React.Fragment key={index}>
        {line}
        {index < content.split('\n').length - 1 && <br />}
      </React.Fragment>
    ));
  };

  return (
    <Box sx={{ height: 'calc(100vh - 120px)', display: 'flex', flexDirection: 'column' }}>
      <Typography variant="h5" gutterBottom>
        AI Diet Advisor
      </Typography>
      <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
        Get personalized nutrition advice and meal suggestions based on your profile
      </Typography>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError('')}>
          {error}
        </Alert>
      )}

      <Paper
        elevation={2}
        sx={{
          flex: 1,
          display: 'flex',
          flexDirection: 'column',
          overflow: 'hidden',
          bgcolor: 'background.default',
        }}
      >
        <List
          sx={{
            flex: 1,
            overflow: 'auto',
            p: 2,
          }}
        >
          {messages.map((message, index) => (
            <ListItem
              key={index}
              sx={{
                display: 'flex',
                justifyContent: message.role === 'user' ? 'flex-end' : 'flex-start',
                alignItems: 'flex-start',
                mb: 2,
                p: 0,
              }}
            >
              {message.role === 'assistant' && (
                <Avatar
                  sx={{
                    bgcolor: 'primary.main',
                    mr: 1,
                    width: 36,
                    height: 36,
                  }}
                >
                  <SmartToyIcon fontSize="small" />
                </Avatar>
              )}
              <Paper
                elevation={1}
                sx={{
                  p: 2,
                  maxWidth: '70%',
                  bgcolor: message.role === 'user' ? 'primary.main' : 'white',
                  color: message.role === 'user' ? 'white' : 'text.primary',
                  borderRadius: 2,
                  borderTopLeftRadius: message.role === 'assistant' ? 0 : 2,
                  borderTopRightRadius: message.role === 'user' ? 0 : 2,
                }}
              >
                <Typography variant="body1" sx={{ whiteSpace: 'pre-wrap' }}>
                  {formatMessage(message.content)}
                </Typography>
              </Paper>
              {message.role === 'user' && (
                <Avatar
                  sx={{
                    bgcolor: 'secondary.main',
                    ml: 1,
                    width: 36,
                    height: 36,
                  }}
                >
                  <PersonIcon fontSize="small" />
                </Avatar>
              )}
            </ListItem>
          ))}
          {loading && (
            <ListItem
              sx={{
                display: 'flex',
                justifyContent: 'flex-start',
                alignItems: 'flex-start',
                mb: 2,
                p: 0,
              }}
            >
              <Avatar
                sx={{
                  bgcolor: 'primary.main',
                  mr: 1,
                  width: 36,
                  height: 36,
                }}
              >
                <SmartToyIcon fontSize="small" />
              </Avatar>
              <Paper
                elevation={1}
                sx={{
                  p: 2,
                  bgcolor: 'white',
                  borderRadius: 2,
                  borderTopLeftRadius: 0,
                  display: 'flex',
                  alignItems: 'center',
                  gap: 1,
                }}
              >
                <CircularProgress size={20} />
                <Typography variant="body2" color="text.secondary">
                  Thinking...
                </Typography>
              </Paper>
            </ListItem>
          )}
          <div ref={messagesEndRef} />
        </List>

        <Box
          sx={{
            p: 2,
            borderTop: 1,
            borderColor: 'divider',
            bgcolor: 'white',
          }}
        >
          <Box sx={{ display: 'flex', gap: 1 }}>
            <TextField
              fullWidth
              multiline
              maxRows={4}
              placeholder="Ask me about nutrition, diet tips, or meal suggestions..."
              value={input}
              onChange={(e) => setInput(e.target.value)}
              onKeyPress={handleKeyPress}
              disabled={loading}
              size="small"
              sx={{
                '& .MuiOutlinedInput-root': {
                  borderRadius: 2,
                },
              }}
            />
            <IconButton
              color="primary"
              onClick={handleSend}
              disabled={!input.trim() || loading}
              sx={{
                bgcolor: 'primary.main',
                color: 'white',
                '&:hover': {
                  bgcolor: 'primary.dark',
                },
                '&:disabled': {
                  bgcolor: 'action.disabledBackground',
                  color: 'action.disabled',
                },
              }}
            >
              <SendIcon />
            </IconButton>
          </Box>
        </Box>
      </Paper>
    </Box>
  );
};

export default Advice;
