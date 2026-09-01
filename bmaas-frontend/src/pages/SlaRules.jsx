import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { slaRulesAPI } from '../api/api';

function SlaRules() {
  const [rules, setRules] = useState([]);
  const [loading, setLoading] = useState(true);
  const [editingRule, setEditingRule] = useState(null);
  const [formData, setFormData] = useState({
    durationHours: '',
    warningThresholdPercent: 80,
  });
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState({ type: '', text: '' });

  const user = JSON.parse(localStorage.getItem('user') || '{}');

  useEffect(() => {
    fetchRules();
  }, []);

  const fetchRules = async () => {
    setLoading(true);
    try {
      const res = await slaRulesAPI.getAll();
      // Sort in order of severity/priority
      const order = { CRITICAL: 1, HIGH: 2, MEDIUM: 3, LOW: 4 };
      const sorted = (res.data.data || []).sort(
        (a, b) => (order[a.priority] || 99) - (order[b.priority] || 99)
      );
      setRules(sorted);
    } catch (err) {
      console.error('Error fetching SLA rules:', err);
      setMessage({ type: 'error', text: 'Failed to load SLA rules.' });
    } finally {
      setLoading(false);
    }
  };

  const handleEditClick = (rule) => {
    setEditingRule(rule);
    setFormData({
      durationHours: rule.durationHours,
      warningThresholdPercent: rule.warningThresholdPercent || 80,
    });
    setMessage({ type: '', text: '' });
  };

  const handleCloseModal = () => {
    setEditingRule(null);
    setMessage({ type: '', text: '' });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!formData.durationHours || formData.durationHours < 1) {
      setMessage({ type: 'error', text: 'Duration must be at least 1 hour.' });
      return;
    }

    setSaving(true);
    setMessage({ type: '', text: '' });

    try {
      await slaRulesAPI.update(editingRule.id, {
        durationHours: parseInt(formData.durationHours, 10),
        warningThresholdPercent: parseInt(formData.warningThresholdPercent, 10),
      });
      setMessage({ type: 'success', text: `SLA Rule for ${editingRule.priority} updated successfully!` });
      setEditingRule(null);
      fetchRules();
    } catch (err) {
      console.error('Error updating SLA rule:', err);
      setMessage({ type: 'error', text: err.response?.data?.message || 'Failed to update rule.' });
    } finally {
      setSaving(false);
    }
  };

  const getPriorityBadge = (priority) => {
    const styles = {
      CRITICAL: 'bg-red-100 text-red-800 border-red-200',
      HIGH: 'bg-orange-100 text-orange-800 border-orange-200',
      MEDIUM: 'bg-yellow-100 text-yellow-800 border-yellow-200',
      LOW: 'bg-green-100 text-green-800 border-green-200',
    };
    return (
      <span className={`px-3 py-1 rounded-full text-xs font-bold border ${styles[priority] || 'bg-gray-100 text-gray-800'}`}>
        {priority}
      </span>
    );
  };

  return (
    <div className="min-h-screen bg-gray-50 pb-16">
      {/* Header */}
      <header className="bg-white shadow-sm border-b">
        <div className="max-w-6xl mx-auto px-4 py-4 flex justify-between items-center">
          <div className="flex items-center gap-3">
            <Link to="/" className="text-blue-600 hover:text-blue-800 text-sm font-medium">
              ← Dashboard
            </Link>
            <span className="text-gray-300">/</span>
            <h1 className="text-xl font-bold text-gray-900">SLA Rule Configuration</h1>
          </div>
          <span className="text-xs bg-purple-100 text-purple-800 px-2.5 py-1 rounded font-bold">
            Project Manager
          </span>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-6xl mx-auto px-4 py-8">
        <div className="mb-6">
          <h2 className="text-2xl font-bold text-gray-900">Service Level Agreement (SLA) Policies</h2>
          <p className="text-sm text-gray-500 mt-1">
            Configure resolution deadlines and early warning thresholds for each bug priority level. These rules dynamically govern SLA countdowns and breach alerts across all projects.
          </p>
        </div>

        {message.text && (
          <div
            className={`p-4 rounded-xl mb-6 text-sm font-medium ${
              message.type === 'success'
                ? 'bg-green-50 text-green-800 border border-green-200'
                : 'bg-red-50 text-red-800 border border-red-200'
            }`}
          >
            {message.text}
          </div>
        )}

        {/* Rules Table */}
        <div className="bg-white rounded-xl border border-gray-200 shadow-sm overflow-hidden">
          {loading ? (
            <div className="text-center py-16 text-gray-500">Loading SLA rules...</div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left">
                <thead className="bg-gray-50 border-b text-xs font-semibold text-gray-500 uppercase tracking-wider">
                  <tr>
                    <th className="py-4 px-6">Priority Level</th>
                    <th className="py-4 px-6">Resolution Window</th>
                    <th className="py-4 px-6">Warning Threshold</th>
                    <th className="py-4 px-6">Early Warning Triggers At</th>
                    <th className="py-4 px-6">Last Updated</th>
                    <th className="py-4 px-6 text-right">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100 text-sm">
                  {rules.map((rule) => {
                    const warningHours = (rule.durationHours * (rule.warningThresholdPercent / 100.0)).toFixed(1);
                    return (
                      <tr key={rule.id} className="hover:bg-gray-50/80 transition-colors">
                        <td className="py-4 px-6">{getPriorityBadge(rule.priority)}</td>
                        <td className="py-4 px-6 font-bold text-gray-900">
                          {rule.durationHours} hours
                          <span className="text-xs text-gray-400 block font-normal">
                            ({(rule.durationHours / 24).toFixed(1)} days)
                          </span>
                        </td>
                        <td className="py-4 px-6">
                          <div className="flex items-center gap-2">
                            <span className="font-semibold text-gray-700">{rule.warningThresholdPercent}%</span>
                            <div className="w-20 bg-gray-200 rounded-full h-1.5 overflow-hidden">
                              <div
                                className="bg-yellow-500 h-1.5 rounded-full"
                                style={{ width: `${rule.warningThresholdPercent}%` }}
                              />
                            </div>
                          </div>
                        </td>
                        <td className="py-4 px-6 text-gray-600 text-xs font-medium">
                          After <strong className="text-yellow-700">{warningHours}h</strong> (at {rule.warningThresholdPercent}% of SLA)
                        </td>
                        <td className="py-4 px-6 text-gray-400 text-xs">
                          {rule.updatedAt ? new Date(rule.updatedAt).toLocaleDateString() : 'System Default'}
                        </td>
                        <td className="py-4 px-6 text-right">
                          <button
                            onClick={() => handleEditClick(rule)}
                            className="bg-blue-50 hover:bg-blue-100 text-blue-700 px-3 py-1.5 rounded-lg text-xs font-semibold transition-colors"
                          >
                            Edit Rule
                          </button>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </main>

      {/* Edit Modal */}
      {editingRule && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-xl max-w-md w-full p-6 shadow-xl border border-gray-200 animate-in fade-in zoom-in duration-150">
            <div className="flex justify-between items-center mb-4">
              <h3 className="text-lg font-bold text-gray-900">
                Edit SLA Policy: {editingRule.priority}
              </h3>
              <button
                onClick={handleCloseModal}
                className="text-gray-400 hover:text-gray-600 text-lg font-bold"
              >
                ✕
              </button>
            </div>

            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="block text-sm font-semibold text-gray-700 mb-1">
                  Resolution Duration (Hours)
                </label>
                <input
                  type="number"
                  min="1"
                  max="720"
                  value={formData.durationHours}
                  onChange={(e) => setFormData({ ...formData, durationHours: e.target.value })}
                  className="w-full px-3.5 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500"
                  required
                />
                <p className="text-xs text-gray-500 mt-1">
                  Total continuous time allowed before the bug breaches SLA.
                </p>
              </div>

              <div>
                <label className="block text-sm font-semibold text-gray-700 mb-1">
                  Warning Threshold (% of duration)
                </label>
                <div className="flex items-center gap-3">
                  <input
                    type="range"
                    min="10"
                    max="95"
                    step="5"
                    value={formData.warningThresholdPercent}
                    onChange={(e) =>
                      setFormData({ ...formData, warningThresholdPercent: parseInt(e.target.value, 10) })
                    }
                    className="flex-1"
                  />
                  <span className="text-sm font-bold text-gray-900 w-12 text-right">
                    {formData.warningThresholdPercent}%
                  </span>
                </div>
                <p className="text-xs text-gray-500 mt-1">
                  Bug status flips to <strong>AT RISK</strong> once {formData.warningThresholdPercent}% of the SLA has elapsed.
                </p>
              </div>

              <div className="flex justify-end gap-2 pt-4 border-t">
                <button
                  type="button"
                  onClick={handleCloseModal}
                  className="px-4 py-2 border border-gray-300 rounded-lg text-sm font-semibold text-gray-700 hover:bg-gray-50"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={saving}
                  className="px-5 py-2 bg-blue-600 text-white rounded-lg text-sm font-semibold hover:bg-blue-700 shadow-sm disabled:opacity-50"
                >
                  {saving ? 'Saving...' : 'Save Changes'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

export default SlaRules;
