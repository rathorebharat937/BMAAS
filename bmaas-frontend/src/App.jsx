import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/Login';
import Register from './pages/Register';
import Dashboard from './pages/Dashboard';
import ProjectDetail from './pages/ProjectDetail';
import Developers from './pages/Developers';
import Bugs from './pages/Bugs';
import BugDetail from './pages/BugDetail';
import BugForm from './pages/BugForm';
import SlaRules from './pages/SlaRules';
import DeveloperBugs from './pages/DeveloperBugs';
import DeveloperLeaderboard from './pages/DeveloperLeaderboard';

function PrivateRoute({ children }) {
  const token = localStorage.getItem('token');
  return token ? children : <Navigate to="/login" />;
}

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/" element={
          <PrivateRoute>
            <Dashboard />
          </PrivateRoute>
        } />
        <Route path="/my-bugs" element={
          <PrivateRoute>
            <DeveloperBugs />
          </PrivateRoute>
        } />
        <Route path="/projects/:id" element={
          <PrivateRoute>
            <ProjectDetail />
          </PrivateRoute>
        } />
        <Route path="/projects/:id/bugs" element={
          <PrivateRoute>
            <Bugs />
          </PrivateRoute>
        } />
        <Route path="/projects/:projectId/bugs/new" element={
          <PrivateRoute>
            <BugForm />
          </PrivateRoute>
        } />
        <Route path="/bugs/:id" element={
          <PrivateRoute>
            <BugDetail />
          </PrivateRoute>
        } />
        <Route path="/bugs/:id/edit" element={
          <PrivateRoute>
            <BugForm />
          </PrivateRoute>
        } />
        <Route path="/developers" element={
          <PrivateRoute>
            <Developers />
          </PrivateRoute>
        } />
        <Route path="/leaderboard" element={
          <PrivateRoute>
            <DeveloperLeaderboard />
          </PrivateRoute>
        } />
        <Route path="/sla-rules" element={
          <PrivateRoute>
            <SlaRules />
          </PrivateRoute>
        } />
      </Routes>
    </Router>
  );
}

export default App;