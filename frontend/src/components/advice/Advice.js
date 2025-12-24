import React, { useState, useRef, useEffect } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  TextField,
  IconButton,
  Paper,
  CircularProgress,
  Avatar,
} from '@mui/material';
import SendIcon from '@mui/icons-material/Send';
import SmartToyIcon from '@mui/icons-material/SmartToy';
import PersonIcon from '@mui/icons-material/Person';
import { adviceService } from '../../services/api';

const Advice = () => {
  const [messages, setMessages] = useState([
    {
      id: 1,
      type: 'ai',
      content: "Hello! I can help you with nutrition advice, meal planning, calorie information, and healthy eating tips. How can I assist you today?",
    },
  ]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const messagesEndRef = useRef(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const buildHistory = (currentMessages) => {
    return currentMessages
      .filter((msg) => msg.id !== 1)
      .map((msg) => ({
        role: msg.type === 'user' ? 'user' : 'model',
        content: msg.content,
      }));
  };

  const handleSend = async () => {
    if (!input.trim() || loading) return;

    const userMessage = {
      id: Date.now(),
      type: 'user',
      content: input.trim(),
    };

    const updatedMessages = [...messages, userMessage];
    setMessages(updatedMessages);
    setInput('');
    setLoading(true);

    try {
      const history = buildHistory(messages);
      const response = await adviceService.chat(userMessage.content, history);
      const aiMessage = {
        id: Date.now() + 1,
        type: 'ai',
        content: response.data.response,
      };
      setMessages((prev) => [...prev, aiMessage]);
    } catch (error) {
      console.error('Error getting advice:', error);
      const errorMessage = {
        id: Date.now() + 1,
        type: 'ai',
        content: "I apologize, but I'm having trouble processing your request right now. Please try again later.",
      };
      setMessages((prev) => [...prev, errorMessage]);
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

  return (
    <Box sx={{ height: 'calc(100vh - 120px)', display: 'flex', flexDirection: 'column' }}>
      <Typography variant="h4" gutterBottom>
        Advice
      </Typography>
      <Typography variant="subtitle1" color="textSecondary" gutterBottom>
        Get personalized nutrition advice and diet tips
      </Typography>

      <Card sx={{ flex: 1, display: 'flex', flexDirection: 'column', mt: 2, overflow: 'hidden' }}>
        <CardContent sx={{ flex: 1, overflow: 'auto', display: 'flex', flexDirection: 'column', gap: 2, p: 2 }}>
          {messages.map((message) => (
            <Box
              key={message.id}
              sx={{
                display: 'flex',
                justifyContent: message.type === 'user' ? 'flex-end' : 'flex-start',
                gap: 1,
              }}
            >
              {message.type === 'ai' && (
                <Avatar sx={{ bgcolor: 'primary.main', width: 36, height: 36 }}>
                  <SmartToyIcon fontSize="small" />
                </Avatar>
              )}
              <Paper
                elevation={1}
                sx={{
                  p: 2,
                  maxWidth: '70%',
                  bgcolor: message.type === 'user' ? 'primary.main' : 'grey.100',
                  color: message.type === 'user' ? 'white' : 'text.primary',
                  borderRadius: 2,
                  borderTopLeftRadius: message.type === 'ai' ? 0 : 2,
                  borderTopRightRadius: message.type === 'user' ? 0 : 2,
                }}
              >
                <Typography
                  variant="body1"
                  sx={{
                    whiteSpace: 'pre-wrap',
                    wordBreak: 'break-word',
                  }}
                >
                  {message.content}
                </Typography>
              </Paper>
              {message.type === 'user' && (
                <Avatar sx={{ bgcolor: 'secondary.main', width: 36, height: 36 }}>
                  <PersonIcon fontSize="small" />
                </Avatar>
              )}
            </Box>
          ))}
          {loading && (
            <Box sx={{ display: 'flex', gap: 1, alignItems: 'center' }}>
              <Avatar sx={{ bgcolor: 'primary.main', width: 36, height: 36 }}>
                <SmartToyIcon fontSize="small" />
              </Avatar>
              <Paper
                elevation={1}
                sx={{
                  p: 2,
                  bgcolor: 'grey.100',
                  borderRadius: 2,
                  borderTopLeftRadius: 0,
                }}
              >
                <CircularProgress size={20} />
              </Paper>
            </Box>
          )}
          <div ref={messagesEndRef} />
        </CardContent>

        <Box sx={{ p: 2, borderTop: 1, borderColor: 'divider' }}>
          <Box sx={{ display: 'flex', gap: 1 }}>
            <TextField
              fullWidth
              variant="outlined"
              placeholder="Ask me about nutrition, diet tips, meal planning..."
              value={input}
              onChange={(e) => setInput(e.target.value)}
              onKeyPress={handleKeyPress}
              disabled={loading}
              multiline
              maxRows={3}
              size="small"
            />
            <IconButton
              color="primary"
              onClick={handleSend}
              disabled={!input.trim() || loading}
              sx={{
                bgcolor: 'primary.main',
                color: 'white',
                '&:hover': { bgcolor: 'primary.dark' },
                '&:disabled': { bgcolor: 'grey.300' },
              }}
            >
              <SendIcon />
            </IconButton>
          </Box>
          <Typography variant="caption" color="textSecondary" sx={{ mt: 1, display: 'block' }}>
            Note: This AI provides general advice. Please consult a healthcare professional for personalized medical advice.
          </Typography>
        </Box>
      </Card>
    </Box>
  );
};

export default Advice;
