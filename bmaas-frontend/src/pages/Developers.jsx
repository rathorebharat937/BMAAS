import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { developersAPI } from '../api/api';

function Developers() {
  const navigate = useNavigate();
  const [developers, setDevelopers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [editingDeveloper, setEditingDeveloper] = useState(null);
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    skillsTechStack: '',
  });

  useEffect(() => {
    fetchDevelopers();
  }, []);

  const fetchDevelopers = async () => {
    try {
      const response = await developersAPI.getAll();
      setDevelopers(response.data.data);
    } catch (error) {
      console.error('Error fetching developers:', error);
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
      if (editingDeveloper) {
        await developersAPI.update(editingDeveloper.id, formData);
      } else {
        await developersAPI.create(formData);
      }
      setShowModal(false);
      setEditingDeveloper(null);
      resetForm();
      fetchDevelopers();
    } catch (error) {
      console.error('Error saving developer:', error);
      alert(error.response?.data?.message || 'Error saving developer');
    }
  };

  const handleEdit = (developer) => {
    setEditingDeveloper(developer);
    setFormData({
      name: developer.name,
      email: developer.email,
      skillsTechStack: developer.skillsTechStack || '',
    });
    setShowModal(true);
  };

  const handleDelete = async (id) => {
    if (window.confirm('Are you sure you want to delete this developer?')) {
      try {
        await developersAPI.delete(id);
        fetchDevelopers();
      } catch (error) {
        console.error('Error deleting developer:', error);
        alert(error.response?.data?.message || 'Error deleting developer');
      }
    }
  };

  const resetForm = () => {
    setFormData({
      name: '',
      email: '',
      skillsTechStack: '',
    });
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    navigate('/login');
  };

  const user = JSON.parse(localStorage.getItem('user') || '{}');

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-xl">Loading...</div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-100">
      {/* Header */}
      <header className="bg-white shadow">
        <div className="max-w-7xl mx-auto px-4 py-4 flex justify-between items-center">
          <div className="flex items-center gap-4">
            <Link to="/" className="text-blue-600 hover:underline">← Back to Dashboard</Link>
            <h1 className="text-2xl font-bold text-gray-900">Developer Management</h1>
          </div>
          <div className="flex items-center gap-4">
            <Link
              to="/leaderboard"
              className="bg-indigo-600 hover:bg-indigo-700 text-white text-xs font-semibold px-3.5 py-2 rounded shadow-sm"
            >
              📊 Performance Leaderboard
            </Link>
            <span className="text-gray-600">Welcome, {user.fullName}</span>
            <button
              onClick={handleLogout}
              className="bg-gray-600 text-white px-4 py-2 rounded hover:bg-gray-700"
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
            <h2 className="text-xl font-semibold">All Developers</h2>
            <p className="text-xs text-gray-500 mt-0.5">Manage developer profiles, module mappings, and historical metrics</p>
          </div>
          <button
            onClick={() => { resetForm(); setEditingDeveloper(null); setShowModal(true); }}
            className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700"
          >
            + Add Developer
          </button>
        </div>

        {/* Developers Table */}
        {developers.length === 0 ? (
          <div className="text-center py-12 text-gray-500">
            No developers yet. Add your first developer!
          </div>
        ) : (
          <div className="bg-white rounded-lg shadow-md overflow-hidden">
            <table className="w-full">
              <thead className="bg-gray-50 text-xs text-gray-500 font-semibold uppercase">
                <tr>
                  <th className="text-left py-3 px-4">Name</th>
                  <th className="text-left py-3 px-4">Email</th>
                  <th className="text-left py-3 px-4">User Account</th>
                  <th className="text-left py-3 px-4">Workload</th>
                  <th className="text-left py-3 px-4">Resolved / Fix Rate</th>
                  <th className="text-left py-3 px-4">Modules</th>
                  <th className="text-left py-3 px-4">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200 text-sm">
                {developers.map((developer) => {
                  const m = developer.metrics;
                  return (
                    <tr key={developer.id} className="hover:bg-gray-50">
                      <td className="py-4 px-4 font-semibold text-gray-900">{developer.name}</td>
                      <td className="py-4 px-4 text-gray-600 text-xs">{developer.email}</td>
                      <td className="py-4 px-4">
                        {developer.userId ? (
                          <span className="bg-green-100 text-green-800 text-xs font-semibold px-2.5 py-1 rounded-full border border-green-200">
                            ✓ Linked (@{developer.userUsername})
                          </span>
                        ) : (
                          <span className="bg-gray-100 text-gray-600 text-xs px-2.5 py-1 rounded-full border border-gray-200">
                            Not Linked
                          </span>
                        )}
                      </td>
                      <td className="py-4 px-4">
                        <span className={`px-2.5 py-1 rounded-full text-xs font-bold ${
                          m && m.currentWorkload > 0
                            ? 'bg-blue-100 text-blue-800 border border-blue-200'
                            : 'bg-gray-100 text-gray-600'
                        }`}>
                          {m ? m.currentWorkload : 0} active
                        </span>
                      </td>
                      <td className="py-4 px-4 text-xs">
                        {m && m.bugsResolved > 0 ? (
                          <div>
                            <span className="font-bold text-gray-900">{m.bugsResolved} resolved</span>
                            <span className="text-gray-400 mx-1">•</span>
                            <span className="text-green-700 font-semibold">{m.firstTimeFixRate}% fix</span>
                          </div>
                        ) : (
                          <span className="text-gray-400 italic">No data yet</span>
                        )}
                      </td>
                      <td className="py-4 px-4">
                        <span className="bg-blue-100 text-blue-800 px-2 py-1 rounded text-xs font-semibold">
                          {developer.moduleCount || 0}
                        </span>
                      </td>
                      <td className="py-4 px-4">
                        <div className="flex gap-3 text-xs">
                          <button
                            onClick={() => handleEdit(developer)}
                            className="text-blue-600 hover:text-blue-800 font-semibold"
                          >
                            Edit
                          </button>
                          <button
                            onClick={() => handleDelete(developer.id)}
                            className="text-red-600 hover:text-red-800 font-semibold"
                          >
                            Delete
                          </button>
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </main>

      {/* Modal */}
      {showModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 w-full max-w-md">
            <h3 className="text-xl font-bold mb-4">
              {editingDeveloper ? 'Edit Developer' : 'Add New Developer'}
            </h3>
            <form onSubmit={handleSubmit}>
              <div className="mb-4">
                <label className="block text-sm font-medium mb-1">Name *</label>
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
                <label className="block text-sm font-medium mb-1">Email *</label>
                <input
                  type="email"
                  name="email"
                  value={formData.email}
                  onChange={handleChange}
                  className="w-full px-3 py-2 border rounded"
                  required
                />
              </div>
              <div className="mb-6">
                <label className="block text-sm font-medium mb-1">Skills / Tech Stack</label>
                <textarea
                  name="skillsTechStack"
                  value={formData.skillsTechStack}
                  onChange={handleChange}
                  className="w-full px-3 py-2 border rounded"
                  rows="3"
                  placeholder="e.g., JavaScript, React, Node.js, Python"
                />
              </div>
              <div className="flex gap-2 justify-end">
                <button
                  type="button"
                  onClick={() => { setShowModal(false); setEditingDeveloper(null); }}
                  className="px-4 py-2 border rounded hover:bg-gray-100"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
                >
                  {editingDeveloper ? 'Update' : 'Add'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

export default Developers;