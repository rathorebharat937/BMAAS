import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { projectsAPI } from '../api/api';

function Dashboard() {
  const navigate = useNavigate();
  const [projects, setProjects] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [editingProject, setEditingProject] = useState(null);
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    startDate: '',
    endDate: '',
    technologyStack: '',
    status: 'ACTIVE',
  });

  const user = JSON.parse(localStorage.getItem('user') || '{}');

  useEffect(() => {
    if (user.role === 'DEVELOPER') {
      navigate('/my-bugs');
      return;
    }
    fetchProjects();
  }, []);

  const fetchProjects = async () => {
    try {
      const response = await projectsAPI.getAll();
      setProjects(response.data.data);
    } catch (error) {
      console.error('Error fetching projects:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      if (editingProject) {
        await projectsAPI.update(editingProject.id, formData);
      } else {
        await projectsAPI.create(formData);
      }
      setShowModal(false);
      setEditingProject(null);
      resetForm();
      fetchProjects();
    } catch (error) {
      console.error('Error saving project:', error);
      alert(error.response?.data?.message || 'Error saving project');
    }
  };

  const handleEdit = (project) => {
    setEditingProject(project);
    setFormData({
      name: project.name,
      description: project.description || '',
      startDate: project.startDate,
      endDate: project.endDate || '',
      technologyStack: project.technologyStack || '',
      status: project.status,
    });
    setShowModal(true);
  };

  const handleDelete = async (id) => {
    if (window.confirm('Are you sure you want to delete this project?')) {
      try {
        await projectsAPI.delete(id);
        fetchProjects();
      } catch (error) {
        console.error('Error deleting project:', error);
        alert(error.response?.data?.message || 'Error deleting project');
      }
    }
  };

  const resetForm = () => {
    setFormData({
      name: '',
      description: '',
      startDate: '',
      endDate: '',
      technologyStack: '',
      status: 'ACTIVE',
    });
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    navigate('/login');
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'ACTIVE': return 'bg-green-100 text-green-800';
      case 'ON_HOLD': return 'bg-yellow-100 text-yellow-800';
      case 'COMPLETED': return 'bg-blue-100 text-blue-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-xl">Loading...</div>
      </div>
    );
  }

  const isPM = user.role === 'PROJECT_MANAGER';
  const isTester = user.role === 'TESTER';

  return (
    <div className="min-h-screen bg-gray-100">
      {/* Header */}
      <header className="bg-white shadow">
        <div className="max-w-7xl mx-auto px-4 py-4 flex justify-between items-center">
          <div className="flex items-center gap-3">
            <h1 className="text-2xl font-bold text-gray-900">BMAAS</h1>
            <span className="text-xs bg-blue-100 text-blue-800 px-2.5 py-1 rounded-full font-bold">
              {user.role === 'TESTER' ? 'QA / Tester' : user.role === 'DEVELOPER' ? 'Developer' : 'Project Manager'}
            </span>
          </div>
          <div className="flex items-center gap-4">
            <span className="text-gray-600 text-sm">Welcome, <strong>{user.fullName}</strong></span>
            {isPM && (
              <>
                <Link
                  to="/leaderboard"
                  className="bg-blue-600 text-white text-sm px-3.5 py-2 rounded-lg hover:bg-blue-700 font-medium flex items-center gap-1.5"
                >
                  <span>📊</span> Leaderboard
                </Link>
                <Link
                  to="/sla-rules"
                  className="bg-indigo-600 text-white text-sm px-3.5 py-2 rounded-lg hover:bg-indigo-700 font-medium flex items-center gap-1.5"
                >
                  <span>⏱️</span> SLA Rules
                </Link>
                <Link
                  to="/developers"
                  className="bg-purple-600 text-white text-sm px-3.5 py-2 rounded-lg hover:bg-purple-700 font-medium"
                >
                  Developers
                </Link>
              </>
            )}
            <button
              onClick={handleLogout}
              className="bg-gray-600 text-white text-sm px-3.5 py-2 rounded-lg hover:bg-gray-700 font-medium"
            >
              Logout
            </button>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 py-8">
        <div className="flex justify-between items-center mb-6">
          <div>
            <h2 className="text-xl font-bold text-gray-900">{isTester ? 'All Projects & Bug Repositories' : 'My Projects'}</h2>
            <p className="text-sm text-gray-500 mt-0.5">
              {isTester ? 'Select a project to report or review bugs' : 'Manage your software development projects'}
            </p>
          </div>
          {isPM && (
            <button
              onClick={() => { resetForm(); setEditingProject(null); setShowModal(true); }}
              className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 text-sm font-semibold shadow-sm"
            >
              + New Project
            </button>
          )}
        </div>

        {/* Projects Grid */}
        {projects.length === 0 ? (
          <div className="text-center py-16 bg-white rounded-xl border border-gray-200 shadow-sm text-gray-500">
            <div className="text-3xl mb-2">📁</div>
            <p className="font-medium text-gray-700">No projects found.</p>
            {isPM && <p className="text-sm mt-1">Create your first project to get started!</p>}
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {projects.map((project) => (
              <div key={project.id} className="bg-white rounded-xl border border-gray-200 shadow-sm p-6 flex flex-col justify-between hover:shadow-md transition-shadow">
                <div>
                  <div className="flex justify-between items-start mb-3">
                    <h3 className="text-lg font-bold text-gray-900">{project.name}</h3>
                    <span className={`px-2.5 py-0.5 rounded-full text-xs font-semibold ${getStatusColor(project.status)}`}>
                      {project.status}
                    </span>
                  </div>
                  <p className="text-gray-600 text-sm mb-4 line-clamp-2">{project.description || 'No description provided'}</p>
                  <div className="text-xs text-gray-500 space-y-1 mb-6 bg-gray-50 p-3 rounded-lg border border-gray-100">
                    <div><strong className="text-gray-700">Start:</strong> {project.startDate} {project.endDate ? `| End: ${project.endDate}` : ''}</div>
                    {project.technologyStack && <div><strong className="text-gray-700">Tech:</strong> {project.technologyStack}</div>}
                    {project.projectManagerName && <div><strong className="text-gray-700">Manager:</strong> {project.projectManagerName}</div>}
                  </div>
                </div>

                {/* Actions */}
                <div className="space-y-2 pt-2 border-t border-gray-100">
                  <div className="flex gap-2">
                    <Link
                      to={`/projects/${project.id}/bugs`}
                      className="flex-1 bg-red-600 text-white text-center py-2 rounded-lg hover:bg-red-700 text-sm font-semibold shadow-sm flex items-center justify-center gap-1"
                    >
                      <span>🐛</span> Bugs
                    </Link>
                    {isPM && (
                      <Link
                        to={`/projects/${project.id}`}
                        className="flex-1 bg-blue-600 text-white text-center py-2 rounded-lg hover:bg-blue-700 text-sm font-semibold"
                      >
                        Modules
                      </Link>
                    )}
                  </div>
                  {isPM && (
                    <div className="flex gap-2">
                      <button
                        onClick={() => handleEdit(project)}
                        className="flex-1 bg-gray-100 text-gray-700 py-1.5 rounded-lg hover:bg-gray-200 text-xs font-medium"
                      >
                        Edit
                      </button>
                      <button
                        onClick={() => handleDelete(project.id)}
                        className="flex-1 bg-red-50 text-red-700 py-1.5 rounded-lg hover:bg-red-100 text-xs font-medium"
                      >
                        Delete
                      </button>
                    </div>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </main>

      {/* Modal */}
      {showModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 w-full max-w-lg max-h-[90vh] overflow-y-auto">
            <h3 className="text-xl font-bold mb-4">
              {editingProject ? 'Edit Project' : 'Create New Project'}
            </h3>
            <form onSubmit={handleSubmit}>
              <div className="mb-4">
                <label className="block text-sm font-medium mb-1">Project Name *</label>
                <input
                  type="text"
                  name="name"
                  value={formData.name}
                  onChange={handleChange}
                  className="w-full px-3 py-2 border rounded"
                  required
                />
              </div>
              <div className="mb-4">
                <label className="block text-sm font-medium mb-1">Description</label>
                <textarea
                  name="description"
                  value={formData.description}
                  onChange={handleChange}
                  className="w-full px-3 py-2 border rounded"
                  rows="3"
                />
              </div>
              <div className="grid grid-cols-2 gap-4 mb-4">
                <div>
                  <label className="block text-sm font-medium mb-1">Start Date *</label>
                  <input
                    type="date"
                    name="startDate"
                    value={formData.startDate}
                    onChange={handleChange}
                    className="w-full px-3 py-2 border rounded"
                    required
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium mb-1">End Date</label>
                  <input
                    type="date"
                    name="endDate"
                    value={formData.endDate}
                    onChange={handleChange}
                    className="w-full px-3 py-2 border rounded"
                  />
                </div>
              </div>
              <div className="mb-4">
                <label className="block text-sm font-medium mb-1">Technology Stack</label>
                <input
                  type="text"
                  name="technologyStack"
                  value={formData.technologyStack}
                  onChange={handleChange}
                  className="w-full px-3 py-2 border rounded"
                  placeholder="e.g., React, Node.js, PostgreSQL"
                />
              </div>
              <div className="mb-6">
                <label className="block text-sm font-medium mb-1">Status *</label>
                <select
                  name="status"
                  value={formData.status}
                  onChange={handleChange}
                  className="w-full px-3 py-2 border rounded"
                >
                  <option value="ACTIVE">Active</option>
                  <option value="ON_HOLD">On Hold</option>
                  <option value="COMPLETED">Completed</option>
                </select>
              </div>
              <div className="flex gap-2 justify-end">
                <button
                  type="button"
                  onClick={() => { setShowModal(false); setEditingProject(null); }}
                  className="px-4 py-2 border rounded hover:bg-gray-100"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
                >
                  {editingProject ? 'Update' : 'Create'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

export default Dashboard;