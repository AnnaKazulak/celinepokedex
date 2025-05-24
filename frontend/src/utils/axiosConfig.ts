import axios from 'axios';
import { TIMEOUTS } from './constants';
import { eventBus } from './eventBus';

// Create a custom axios instance with increased timeouts
const axiosInstance = axios.create({
  // Use relative URLs - these will be properly handled by the Vite proxy
  baseURL: '/api',
  // Default timeout is 60 seconds (60000ms), much longer than the standard 0ms (no timeout)
  timeout: TIMEOUTS.API_REQUEST_TIMEOUT,
  // Add withCredentials to allow cross-origin requests with credentials
  withCredentials: true
});

// Add request interceptor for logging
axiosInstance.interceptors.request.use(
  config => {
    // For image generation requests, use an even longer timeout
    if (config.url?.includes('/images/generate')) {
      config.timeout = TIMEOUTS.IMAGE_GENERATION_TIMEOUT; // Use the specific image generation timeout
    }
    
    // Check for JWT token in localStorage or sessionStorage
    const userSession = JSON.parse(
      localStorage.getItem('celinepokedex_user_session') || 
      sessionStorage.getItem('celinepokedex_user_session') || 
      'null'
    );
    
    // If token exists, add it to the Authorization header
    if (userSession && userSession.token) {
      config.headers.Authorization = `Bearer ${userSession.token}`;
    }
    
    return config;
  },
  error => {
    return Promise.reject(error);
  }
);

// Add response interceptor for error handling
axiosInstance.interceptors.response.use(
  response => {
    return response;
  },
  error => {
    // Handle 401 Unauthorized errors - session expired or invalid token
    if (error.response && error.response.status === 401) {
      // Clear user session from storage
      localStorage.removeItem('celinepokedex_user_session');
      sessionStorage.removeItem('celinepokedex_user_session');
      
      // Emit event for the app to handle login status update
      eventBus.emit('session-expired', {
        message: 'Session abgelaufen. Bitte neu einloggen.'
      });
      
      console.warn('401 Unauthorized: Session expired or invalid token');
    }
    // Handle timeout errors specifically (keep existing functionality)
    else if (error.code === 'ECONNABORTED' || (error.response && error.response.status === 504)) {
      console.warn('Request timed out or gateway timeout occurred');
    }
    return Promise.reject(error);
  }
);

export default axiosInstance;