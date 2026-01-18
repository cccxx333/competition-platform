import React from 'react';
import { Navigate } from 'react-router-dom';
import { Spin } from 'antd';

const ProtectedRoute = ({ children }) => {
  const token = localStorage.getItem('token');
  const [loading, setLoading] = React.useState(true);
  const [isValid, setIsValid] = React.useState(false);

  React.useEffect(() => {
    validateToken();
  }, []);

  const validateToken = async () => {
    if (!token) {
      setLoading(false);
      return;
    }

    try {
      // 这里可以调用API验证token有效性
      // const response = await userAPI.validateToken();
      setIsValid(true);
    } catch (error) {
      localStorage.removeItem('token');
      setIsValid(false);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div style={{ 
        display: 'flex', 
        justifyContent: 'center', 
        alignItems: 'center', 
        height: '100vh' 
      }}>
        <Spin size="large" />
      </div>
    );
  }

  return token && isValid ? children : <Navigate to="/login" replace />;
};

export default ProtectedRoute;