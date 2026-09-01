import { useState, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { projectsAPI, modulesAPI, bugsAPI } from '../api/api';

function Bugs() {
  const { id: projectId } = useParams();
  const navigate = useNavigate();
  const [project, setProject] = useState(null);
  const [modules, setModules] = useState([]);
  const [bugs, setBugs] = useState([]);
  const [slaSummary, setSlaSummary] = useState({ onTrack: 0, atRisk: 0, breached: 0, total: 0 });
  const [loading, setLoading] = useState(true);

  // Filter states
  const [selectedModule, setSelectedModule] = useState('');
  const [selectedStatus, setSelectedStatus] = useState('');
  const [selectedSeverity, setSelectedSeverity] = useState('');
  const [selectedSlaStatus, setSelectedSlaStatus] = useState('');

  const user = JSON.parse(localStorage.getItem('user') || '{}');
  const isPM = user.role === 'PROJECT_MANAGER';

  useEffect(() => {
    fetchInitialData();
  }, [projectId]);

  useEffect(() => {
    fetchBugs();
  }, [projectId, selectedModule, selectedStatus, selectedSeverity, selectedSlaStatus]);

  const fetchInitialData = async () => {
    try {
      const [projectRes, modulesRes, slaRes] = await Promise.all([
        projectsAPI.getById(projectId),
        modulesAPI.getByProject(projectId),
        bugsAPI.getSlaSummary(projectId).catch(() => ({ data: { data: { onTrack: 0, atRisk: 0, breached: 0, total: 0 } } })),
      ]);
      setProject(projectRes.data.data);
      setModules(modulesRes.data.data);
      setSlaSummary(slaRes.data.data);
    } catch (error) {
      console.error('Error fetching project data:', error);
      navigate('/');
    }
  };

  const fetchBugs = async () => {
    setLoading(true);
    try {
      const params = {};
      if (selectedModule) params.moduleId = selectedModule;
      if (selectedStatus) params.status = selectedStatus;
      if (selectedSeverity) params.severity = selectedSeverity;
      if (selectedSlaStatus) params.slaStatus = selectedSlaStatus;

      const [bugsRes, slaRes] = await Promise.all([
        bugsAPI.getByProject(projectId, params),
        bugsAPI.getSlaSummary(projectId).catch(() => null),
      ]);
      setBugs(bugsRes.data.data);
      if (slaRes?.data?.data) {
        setSlaSummary(slaRes.data.data);
      }
    } catch (error) {
      console.error('Error fetching bugs:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteBug = async (bugId) => {
    if (window.confirm('Are you sure you want to delete this bug?')) {
      try {
        await bugsAPI.delete(bugId);
        fetchBugs();
      } catch (error) {
        console.error('Error deleting bug:', error);
        alert(error.response?.data?.message || 'Error deleting bug');
      }
    }
  };

  const clearFilters = () => {
    setSelectedModule('');
    setSelectedStatus('');
    setSelectedSeverity('');
    setSelectedSlaStatus('');
  };

  const getStatusBadge = (status) => {
    const styles = {
      OPEN: 'bg-yellow-100 text-yellow-800 border-yellow-200',
      IN_PROGRESS: 'bg-blue-100 text-blue-800 border-blue-200',
      RESOLVED: 'bg-purple-100 text-purple-800 border-purple-200',
      CLOSED: 'bg-green-100 text-green-800 border-green-200',
      REOPENED: 'bg-orange-100 text-orange-800 border-orange-200',
      REJECTED: 'bg-gray-100 text-gray-800 border-gray-200',
    };
    return (
      <span className={`px-2.5 py-0.5 rounded-full text-xs font-semibold border ${styles[status] || 'bg-gray-100 text-gray-800'}`}>
        {status}
      </span>
    );
  };

  const getSlaBadge = (slaStatus) => {
    const styles = {
      ON_TRACK: 'bg-emerald-100 text-emerald-800 border-emerald-300',
      AT_RISK: 'bg-amber-100 text-amber-900 border-amber-300 animate-pulse',
      BREACHED: 'bg-rose-100 text-rose-800 border-rose-300 font-bold',
    };
    const labels = {
      ON_TRACK: 'On Track',
      AT_RISK: '⚠️ At Risk',
      BREACHED: '🚨 Breached',
    };
    return (
      <span className={`px-2.5 py-0.5 rounded-full text-xs font-semibold border ${styles[slaStatus] || 'bg-gray-100 text-gray-800'}`}>
        {labels[slaStatus] || slaStatus}
      </span>
    );
  };

  const getSeverityBadge = (severity) => {
    const styles = {
      CRITICAL: 'bg-red-600 text-white',
      HIGH: 'bg-orange-500 text-white',
      MEDIUM: 'bg-yellow-500 text-white',
      LOW: 'bg-green-600 text-white',
    };
    return (
      <span className={`px-2 py-0.5 rounded text-xs font-bold ${styles[severity] || 'bg-gray-500 text-white'}`}>
        {severity}
      </span>
    );
  };

  const getPriorityBadge = (priority) => {
    const colors = {
      CRITICAL: 'text-red-600 font-bold',
      HIGH: 'text-orange-600 font-bold',
      MEDIUM: 'text-yellow-600 font-medium',
      LOW: 'text-green-600 font-medium',
    };
    return <span className={`text-xs ${colors[priority] || 'text-gray-600'}`}>{priority}</span>;
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow-sm border-b">
        <div className="max-w-7xl mx-auto px-4 py-4 flex justify-between items-center">
          <div className="flex items-center gap-4">
            <Link to="/" className="text-blue-600 hover:text-blue-800 text-sm font-medium">
              ← Dashboard
            </Link>
            {project && (
              <>
                <span className="text-gray-300">/</span>
                {isPM && (
                  <>
                    <Link to={`/projects/${projectId}`} className="text-blue-600 hover:text-blue-800 text-sm font-medium">
                      {project.name}
                    </Link>
                    <span className="text-gray-300">/</span>
                  </>
                )}
                <h1 className="text-xl font-bold text-gray-900">Bugs ({project.name})</h1>
              </>
            )}
          </div>
          <div className="flex items-center gap-3">
            {isPM && (
              <Link
                to="/sla-rules"
                className="bg-gray-100 hover:bg-gray-200 text-gray-800 px-3 py-2 rounded-lg text-xs font-semibold"
              >
                ⚙️ SLA Rules
              </Link>
            )}
            <span className="text-xs bg-gray-100 text-gray-700 px-2 py-1 rounded font-medium">
              {user.role}
            </span>
            <Link
              to={`/projects/${projectId}/bugs/new`}
              className="bg-red-600 text-white px-4 py-2 rounded-lg hover:bg-red-700 text-sm font-semibold shadow-sm flex items-center gap-1.5"
            >
              <span>+</span> Report Bug
            </Link>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 py-6">
        {/* SLA & Status Summary Strip */}
        <div className="grid grid-cols-2 sm:grid-cols-4 lg:grid-cols-7 gap-3 mb-6">
          <div className="bg-white p-3.5 rounded-xl border border-gray-200 shadow-sm">
            <div className="text-xs font-semibold text-gray-500 uppercase">Total Bugs</div>
            <div className="text-2xl font-bold text-gray-900 mt-1">{bugs.length}</div>
          </div>
          <div className="bg-white p-3.5 rounded-xl border border-yellow-200 bg-yellow-50/20 shadow-sm">
            <div className="text-xs font-semibold text-yellow-700 uppercase">Open</div>
            <div className="text-2xl font-bold text-yellow-800 mt-1">
              {bugs.filter(b => b.status === 'OPEN').length}
            </div>
          </div>
          <div className="bg-white p-3.5 rounded-xl border border-blue-200 bg-blue-50/20 shadow-sm">
            <div className="text-xs font-semibold text-blue-700 uppercase">In Progress</div>
            <div className="text-2xl font-bold text-blue-800 mt-1">
              {bugs.filter(b => b.status === 'IN_PROGRESS').length}
            </div>
          </div>
          <div className="bg-white p-3.5 rounded-xl border border-emerald-200 bg-emerald-50/30 shadow-sm">
            <div className="text-xs font-semibold text-emerald-700 uppercase">SLA On Track</div>
            <div className="text-2xl font-bold text-emerald-800 mt-1">{slaSummary.onTrack}</div>
          </div>
          <div className="bg-white p-3.5 rounded-xl border border-amber-300 bg-amber-50/40 shadow-sm">
            <div className="text-xs font-semibold text-amber-800 uppercase">SLA At Risk</div>
            <div className="text-2xl font-bold text-amber-800 mt-1">{slaSummary.atRisk}</div>
          </div>
          <div className="bg-white p-3.5 rounded-xl border border-rose-300 bg-rose-50/40 shadow-sm">
            <div className="text-xs font-semibold text-rose-800 uppercase">SLA Breached</div>
            <div className="text-2xl font-bold text-rose-800 mt-1">{slaSummary.breached}</div>
          </div>
          <div className="bg-white p-3.5 rounded-xl border border-purple-200 bg-purple-50/20 shadow-sm">
            <div className="text-xs font-semibold text-purple-700 uppercase">Resolved/Closed</div>
            <div className="text-2xl font-bold text-purple-800 mt-1">
              {bugs.filter(b => b.status === 'RESOLVED' || b.status === 'CLOSED').length}
            </div>
          </div>
        </div>

        {/* Filter Toolbar */}
        <div className="bg-white p-4 rounded-xl border border-gray-200 shadow-sm mb-6 flex flex-wrap gap-4 items-center justify-between">
          <div className="flex flex-wrap gap-3 items-center flex-1">
            {/* Module filter */}
            <div>
              <label className="block text-xs font-medium text-gray-500 mb-1">Module</label>
              <select
                value={selectedModule}
                onChange={(e) => setSelectedModule(e.target.value)}
                className="px-3 py-1.5 border border-gray-300 rounded-lg text-sm bg-white focus:ring-2 focus:ring-blue-500"
              >
                <option value="">All Modules</option>
                {modules.map((m) => (
                  <option key={m.id} value={m.id}>
                    {m.name}
                  </option>
                ))}
              </select>
            </div>

            {/* Status filter */}
            <div>
              <label className="block text-xs font-medium text-gray-500 mb-1">Status</label>
              <select
                value={selectedStatus}
                onChange={(e) => setSelectedStatus(e.target.value)}
                className="px-3 py-1.5 border border-gray-300 rounded-lg text-sm bg-white focus:ring-2 focus:ring-blue-500"
              >
                <option value="">All Statuses</option>
                <option value="OPEN">Open</option>
                <option value="IN_PROGRESS">In Progress</option>
                <option value="RESOLVED">Resolved</option>
                <option value="CLOSED">Closed</option>
                <option value="REOPENED">Reopened</option>
                <option value="REJECTED">Rejected</option>
              </select>
            </div>

            {/* Severity filter */}
            <div>
              <label className="block text-xs font-medium text-gray-500 mb-1">Severity</label>
              <select
                value={selectedSeverity}
                onChange={(e) => setSelectedSeverity(e.target.value)}
                className="px-3 py-1.5 border border-gray-300 rounded-lg text-sm bg-white focus:ring-2 focus:ring-blue-500"
              >
                <option value="">All Severities</option>
                <option value="CRITICAL">Critical</option>
                <option value="HIGH">High</option>
                <option value="MEDIUM">Medium</option>
                <option value="LOW">Low</option>
              </select>
            </div>

            {/* SLA Status filter */}
            <div>
              <label className="block text-xs font-medium text-gray-500 mb-1">SLA Status</label>
              <select
                value={selectedSlaStatus}
                onChange={(e) => setSelectedSlaStatus(e.target.value)}
                className="px-3 py-1.5 border border-gray-300 rounded-lg text-sm bg-white focus:ring-2 focus:ring-blue-500"
              >
                <option value="">All SLA Statuses</option>
                <option value="ON_TRACK">On Track</option>
                <option value="AT_RISK">At Risk (Warning)</option>
                <option value="BREACHED">Breached</option>
              </select>
            </div>

            {(selectedModule || selectedStatus || selectedSeverity || selectedSlaStatus) && (
              <button
                onClick={clearFilters}
                className="mt-5 text-sm text-gray-600 hover:text-red-600 font-medium underline"
              >
                Clear Filters
              </button>
            )}
          </div>
        </div>

        {/* Bug List Table */}
        <div className="bg-white rounded-xl border border-gray-200 shadow-sm overflow-hidden">
          {loading ? (
            <div className="text-center py-16 text-gray-500">Loading bugs...</div>
          ) : bugs.length === 0 ? (
            <div className="text-center py-16">
              <div className="text-4xl mb-3">🐛</div>
              <h3 className="text-lg font-semibold text-gray-800 mb-1">No bugs found</h3>
              <p className="text-sm text-gray-500 mb-4">
                {selectedModule || selectedStatus || selectedSeverity || selectedSlaStatus
                  ? 'Try changing or clearing your filter criteria.'
                  : 'No bugs have been reported for this project yet.'}
              </p>
              <Link
                to={`/projects/${projectId}/bugs/new`}
                className="inline-block bg-red-600 text-white px-4 py-2 rounded-lg text-sm font-semibold hover:bg-red-700"
              >
                Report First Bug
              </Link>
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left">
                <thead className="bg-gray-50 border-b text-xs font-semibold text-gray-500 uppercase tracking-wider">
                  <tr>
                    <th className="py-3.5 px-4">Bug ID</th>
                    <th className="py-3.5 px-4">Title</th>
                    <th className="py-3.5 px-4">Module</th>
                    <th className="py-3.5 px-4">Priority / Severity</th>
                    <th className="py-3.5 px-4">Status</th>
                    <th className="py-3.5 px-4">SLA State</th>
                    <th className="py-3.5 px-4">SLA Remaining</th>
                    <th className="py-3.5 px-4">Reporter</th>
                    <th className="py-3.5 px-4 text-right">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100 text-sm">
                  {bugs.map((bug) => {
                    const isBreached = bug.slaStatus === 'BREACHED';
                    const isAtRisk = bug.slaStatus === 'AT_RISK';
                    return (
                      <tr
                        key={bug.id}
                        className={`hover:bg-gray-50/80 transition-colors ${
                          isBreached ? 'bg-red-50/20' : isAtRisk ? 'bg-amber-50/20' : ''
                        }`}
                      >
                        <td className="py-3.5 px-4 font-mono text-xs text-gray-500">#{bug.id}</td>
                        <td className="py-3.5 px-4 font-medium text-gray-900">
                          <Link
                            to={`/bugs/${bug.id}`}
                            className="text-blue-600 hover:text-blue-800 hover:underline font-semibold flex items-center gap-1.5"
                          >
                            {isBreached && <span title="SLA Breached">🚨</span>}
                            {bug.title}
                          </Link>
                          {bug.environment && (
                            <div className="text-xs text-gray-400 mt-0.5">Env: {bug.environment}</div>
                          )}
                        </td>
                        <td className="py-3.5 px-4 text-gray-600 font-medium">{bug.moduleName}</td>
                        <td className="py-3.5 px-4">
                          <div className="flex flex-col gap-1 items-start">
                            {getPriorityBadge(bug.priority)}
                            {getSeverityBadge(bug.severity)}
                          </div>
                        </td>
                        <td className="py-3.5 px-4">{getStatusBadge(bug.status)}</td>
                        <td className="py-3.5 px-4">{getSlaBadge(bug.slaStatus)}</td>
                        <td className="py-3.5 px-4 font-mono text-xs">
                          {isBreached ? (
                            <span className="text-rose-600 font-bold">{bug.slaRemainingTime}</span>
                          ) : isAtRisk ? (
                            <span className="text-amber-700 font-bold">{bug.slaRemainingTime}</span>
                          ) : (
                            <span className="text-gray-600">{bug.slaRemainingTime || '-'}</span>
                          )}
                        </td>
                        <td className="py-3.5 px-4 text-gray-600 text-xs">{bug.reporterName}</td>
                        <td className="py-3.5 px-4 text-right">
                          <div className="flex justify-end items-center gap-2">
                            <Link
                              to={`/bugs/${bug.id}`}
                              className="bg-gray-100 hover:bg-gray-200 text-gray-700 px-2.5 py-1 rounded text-xs font-medium"
                            >
                              View
                            </Link>
                            <Link
                              to={`/bugs/${bug.id}/edit`}
                              className="bg-blue-50 hover:bg-blue-100 text-blue-700 px-2.5 py-1 rounded text-xs font-medium"
                            >
                              Edit
                            </Link>
                            {isPM && (
                              <button
                                onClick={() => handleDeleteBug(bug.id)}
                                className="bg-red-50 hover:bg-red-100 text-red-700 px-2.5 py-1 rounded text-xs font-medium"
                              >
                                Delete
                              </button>
                            )}
                          </div>
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
    </div>
  );
}

export default Bugs;
