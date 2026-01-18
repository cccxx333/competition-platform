import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { ConfigProvider } from 'antd';
import zhCN from 'antd/locale/zh_CN';
import AppLayout from './components/Layout/AppLayout';
import Login from './pages/Login/Login';
import Home from './pages/Home/Home';
import CompetitionList from './pages/Competitions/CompetitionList';
import TeamList from './pages/Teams/TeamList';
import Recommendations from './pages/Recommendations/Recommendations';
import Profile from './pages/Profile/Profile';
import 'antd/dist/reset.css';
import './App.css';

// 路由守卫组件
const ProtectedRoute = ({ children }) => {
  const token = localStorage.getItem('token');
  return token ? children : <Navigate to="/login" replace />;
};

function App() {
  return (
    <ConfigProvider locale={zhCN}>
      <Router>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/*" element={
            <ProtectedRoute>
              <AppLayout>
                <Routes>
                  <Route path="/" element={<Home />} />
                  <Route path="/competitions" element={<CompetitionList />} />
                  <Route path="/teams" element={<TeamList />} />
                  <Route path="/recommendations" element={<Recommendations />} />
                  <Route path="/profile" element={<Profile />} />
                </Routes>
              </AppLayout>
            </ProtectedRoute>
          } />
        </Routes>
      </Router>
    </ConfigProvider>
  );
}

export default App;