import axios from 'axios';
import { TIMEOUTS } from './constants';

// Create a custom axios instance with increased timeouts
const axiosInstance = axios.create({
  // Default timeout is 60 seconds (60000ms), much longer than the standard 0ms (no timeout)
  timeout: TIMEOUTS.API_REQUEST_TIMEOUT
});

// Add request interceptor for logging
axiosInstance.interceptors.request.use(
  config => {
    // For image generation requests, use an even longer timeout
    if (config.url?.includes('/images/generate')) {
      config.timeout = TIMEOUTS.IMAGE_GENERATION_TIMEOUT; // Use the specific image generation timeout
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
    // Handle timeout errors specifically
    if (error.code === 'ECONNABORTED' || (error.response && error.response.status === 504)) {
      console.warn('Request timed out or gateway timeout occurred');
    }
    return Promise.reject(error);
  }
);

export default axiosInstance;