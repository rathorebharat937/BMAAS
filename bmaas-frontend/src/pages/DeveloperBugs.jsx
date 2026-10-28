import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { developersAPI, bugsAPI } from '../api/api';

function DeveloperBugs() {
  const [profile, setProfile] = useState(null);
  const [assignedWork, setAssignedWork] = useState([]);
  const [bugs, setBugs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [resolveModalBug, setResolveModalBug] = useState(null);
  const [resolutionNotes, setResolutionNotes] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [message, setMessage] = useState({ type: '', text: '' });

  // Filter states
  const [selectedStatus, setSelectedStatus] = useState('');
  const [selectedSeverity, setSelectedSeverity] = useState('');
  const [selectedSlaStatus, setSelectedSlaStatus] = useState('');
  const [selectedModuleFilter, setSelectedModuleFilter] = useState('');

  const user = JSON.parse(localStorage.getItem('user') || '{}');

  useEffect(() => {
    fetchProfileWorkAndBugs();
  }, [selectedStatus, selectedSeverity, selectedSlaStatus, selectedModuleFilter]);

  const fetchProfileWorkAndBugs = async () => {
    setLoading(true);
    try {
      const [profRes, workRes, bugsRes] = await Promise.all([
        developersAPI.getMyProfile().catch(() => ({ data: { data: null } })),
        developersAPI.getMyWork().catch(() => ({ data: { data: [] } })),
        developersAPI.getMyBugs({
          status: selectedStatus || undefined,
          severity: selectedSeverity || undefined,
          slaStatus: selectedSlaStatus || undefined,
          moduleId: selectedModuleFilter || undefined,
        }).catch(() => ({ data: { data: [] } })),
      ]);

      setProfile(profRes.data?.data || null);
      setAssignedWork(workRes.data?.data || []);
      setBugs(bugsRes.data?.data || []);
    } catch (err) {
      console.error('Error fetching developer workspace data:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleStartWork = async (bugId) => {
    try {
      await bugsAPI.changeStatus(bugId, 'IN_PROGRESS');
      setMessage({ type: 'success', text: `Bug #${bugId} moved to IN_PROGRESS!` });
      fetchProfileWorkAndBugs();
    } catch (err) {
      setMessage({ type: 'error', text: err.response?.data?.message || 'Failed to update status.' });
    }
  };

  const handleOpenResolveModal = (bug) => {
    setResolveModalBug(bug);
    setResolutionNotes('');
    setMessage({ type: '', text: '' });
  };

  const handleCloseResolveModal = () => {
    setResolveModalBug(null);
    setResolutionNotes('');
  };

  const handleSubmitResolution = async (e) => {
    e.preventDefault();
    if (!resolutionNotes.trim()) {
      setMessage({ type: 'error', text: 'Resolution notes are required.' });
      return;
    }

    setSubmitting(true);
    try {
      await bugsAPI.changeStatus(resolveModalBug.id, 'RESOLVED', resolutionNotes.trim());
      setMessage({ type: 'success', text: `Bug #${resolveModalBug.id} marked as RESOLVED and submitted for verification!` });
      setResolveModalBug(null);
      fetchProfileWorkAndBugs();
    } catch (err) {
      setMessage({ type: 'error', text: err.response?.data?.message || 'Failed to submit resolution.' });
    } finally {
      setSubmitting(false);
    }
  };

  const getStatusBadge = (status) => {
    const styles = {
      OPEN: 'bg-yellow-100 text-yellow-800 border-yellow-200',
      ASSIGNED: 'bg-indigo-100 text-indigo-800 border-indigo-200 font-bold',
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

  const assignedCount = bugs.filter(b => b.status === 'ASSIGNED').length;
  const inProgressCount = bugs.filter(b => b.status === 'IN_PROGRESS').length;
  const resolvedCount = bugs.filter(b => b.status === 'RESOLVED').length;

  return (
    <div className="min-h-screen bg-gray-50 pb-20">
      {/* Header */}
      <header className="bg-white shadow-sm border-b sticky top-0 z-20">
        <div className="max-w-7xl mx-auto px-4 py-3.5 flex justify-between items-center">
          <div className="flex items-center gap-3">
            <h1 className="text-xl font-bold text-gray-900 flex items-center gap-2">
              <span className="text-blue-600">⚡</span>
              <span>BMAAS Developer Workspace</span>
            </h1>
            <span className="text-xs bg-purple-100 text-purple-800 px-2.5 py-0.5 rounded-full font-bold">
              Developer Hub
            </span>
          </div>

          <div className="flex items-center gap-4">
            <nav className="hidden md:flex items-center gap-3 text-sm font-medium">
              <a href="#assigned-work" className="text-blue-600 hover:text-blue-800 flex items-center gap-1">
                <span>📁</span> Assigned Work ({assignedWork.length})
              </a>
              <span className="text-gray-300">|</span>
              <a href="#assigned-bugs" className="text-purple-600 hover:text-purple-800 flex items-center gap-1">
                <span>🐛</span> Assigned Bugs ({bugs.length})
              </a>
              <span className="text-gray-300">|</span>
              <Link to="/leaderboard" className="text-gray-600 hover:text-gray-900">
                🏆 Leaderboard
              </Link>
            </nav>

            <div className="flex items-center gap-3 pl-2 border-l border-gray-200">
              <div className="text-right hidden sm:block">
                <div className="text-sm font-bold text-gray-900">{user.fullName || profile?.name || 'Developer'}</div>
                <div className="text-xs text-gray-500">{user.email || profile?.email}</div>
              </div>
              <button
                onClick={() => {
                  localStorage.removeItem('token');
                  localStorage.removeItem('user');
                  window.location.href = '/login';
                }}
                className="bg-gray-100 hover:bg-gray-200 text-gray-700 text-xs px-3 py-1.5 rounded-lg font-semibold transition"
              >
                Logout
              </button>
            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 py-6 space-y-8">
        {/* Unlinked developer notice if no matching profile */}
        {!loading && !profile && (
          <div className="bg-amber-50 border-2 border-amber-300 text-amber-900 p-5 rounded-xl shadow-sm">
            <div className="flex items-start gap-3">
              <span className="text-2xl">ℹ️</span>
              <div>
                <h3 className="font-bold text-base">Developer Profile Not Linked Yet</h3>
                <p className="text-sm text-amber-800 mt-1">
                  Your login account (<strong>{user.email}</strong>) has not been linked to a Developer record yet. 
                  Please ask your Project Manager to link your email in the Developer Management section.
                </p>
              </div>
            </div>
          </div>
        )}

        {message.text && (
          <div
            className={`p-4 rounded-xl text-sm font-medium ${
              message.type === 'success'
                ? 'bg-green-50 text-green-800 border border-green-200'
                : 'bg-red-50 text-red-800 border border-red-200'
            }`}
          >
            {message.text}
          </div>
        )}

        {/* Top Summary KPI Cards */}
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          <div className="bg-white p-4 rounded-xl border border-blue-200 bg-blue-50/20 shadow-sm">
            <div className="text-xs font-semibold text-blue-700 uppercase flex items-center justify-between">
              <span>📁 Assigned Modules</span>
              <span className="text-base">🚀</span>
            </div>
            <div className="text-3xl font-extrabold text-blue-900 mt-1">{assignedWork.length}</div>
            <div className="text-[11px] text-blue-600 mt-1">Active project assignments</div>
          </div>

          <div className="bg-white p-4 rounded-xl border border-indigo-200 bg-indigo-50/20 shadow-sm">
            <div className="text-xs font-semibold text-indigo-700 uppercase flex items-center justify-between">
              <span>🎯 Assigned Bugs</span>
              <span className="text-base">🐛</span>
            </div>
            <div className="text-3xl font-extrabold text-indigo-900 mt-1">{bugs.length}</div>
            <div className="text-[11px] text-indigo-600 mt-1">{assignedCount} pending action</div>
          </div>

          <div className="bg-white p-4 rounded-xl border border-amber-200 bg-amber-50/20 shadow-sm">
            <div className="text-xs font-semibold text-amber-700 uppercase flex items-center justify-between">
              <span>⏳ In Progress</span>
              <span className="text-base">⚡</span>
            </div>
            <div className="text-3xl font-extrabold text-amber-900 mt-1">{inProgressCount}</div>
            <div className="text-[11px] text-amber-600 mt-1">Currently being fixed</div>
          </div>

          <div className="bg-white p-4 rounded-xl border border-emerald-200 bg-emerald-50/20 shadow-sm">
            <div className="text-xs font-semibold text-emerald-700 uppercase flex items-center justify-between">
              <span>✅ Resolved / Fixed</span>
              <span className="text-base">🎉</span>
            </div>
            <div className="text-3xl font-extrabold text-emerald-900 mt-1">{resolvedCount}</div>
            <div className="text-[11px] text-emerald-600 mt-1">Submitted for verification</div>
          </div>
        </div>

        {/* SECTION 1: ASSIGNED WORK (MODULES & PROJECTS) */}
        <section id="assigned-work" className="space-y-4">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 border-b pb-3">
            <div>
              <div className="flex items-center gap-2">
                <span className="text-xl">📁</span>
                <h2 className="text-lg font-bold text-gray-900">My Assigned Projects & Modules (Assigned Work)</h2>
                <span className="bg-blue-100 text-blue-800 text-xs px-2.5 py-0.5 rounded-full font-bold">
                  {assignedWork.length} {assignedWork.length === 1 ? 'Module' : 'Modules'}
                </span>
              </div>
              <p className="text-xs text-gray-500 mt-0.5">
                Modules and development responsibilities assigned to you by Project Managers.
              </p>
            </div>
          </div>

          {loading ? (
            <div className="bg-white p-8 rounded-xl border border-gray-200 text-center text-gray-500 text-sm">
              Loading your assigned modules and work...
            </div>
          ) : assignedWork.length === 0 ? (
            <div className="bg-white p-8 rounded-xl border border-dashed border-gray-300 text-center">
              <div className="text-3xl mb-2">📋</div>
              <h3 className="text-base font-bold text-gray-800">No project modules assigned yet</h3>
              <p className="text-xs text-gray-500 max-w-md mx-auto mt-1">
                When a Project Manager adds you to a project module team (e.g. as Contributor or Lead), that assigned work will appear here immediately with project specs and team information.
              </p>
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {assignedWork.map((item) => (
                <div
                  key={item.moduleId}
                  className="bg-white rounded-xl border border-gray-200 hover:border-blue-300 shadow-sm p-5 flex flex-col justify-between transition hover:shadow-md"
                >
                  <div className="space-y-3">
                    {/* Top Row: Project & Module Badges */}
                    <div className="flex items-start justify-between gap-3">
                      <div>
                        <div className="flex items-center gap-2 flex-wrap">
                          <span className="text-xs font-bold uppercase tracking-wider text-blue-600 bg-blue-50 px-2 py-0.5 rounded">
                            Project: {item.projectName}
                          </span>
                          <span className="text-xs bg-emerald-50 text-emerald-700 px-2 py-0.5 rounded font-semibold border border-emerald-200">
                            {item.projectStatus}
                          </span>
                        </div>
                        <h3 className="text-lg font-bold text-gray-900 mt-1 flex items-center gap-2">
                          <span>{item.moduleName}</span>
                          <span className="text-xs bg-purple-100 text-purple-800 px-2.5 py-0.5 rounded-full font-bold">
                            {item.roleOnModule || 'Contributor'}
                          </span>
                        </h3>
                      </div>

                      <div className="flex flex-col items-end gap-1">
                        <span className="text-[11px] font-bold px-2 py-0.5 rounded bg-blue-100 text-blue-800">
                          {item.moduleStatus || 'IN_PROGRESS'}
                        </span>
                        <span className="text-[10px] text-gray-500">
                          Priority: <strong>{item.modulePriority || 'MEDIUM'}</strong>
                        </span>
                      </div>
                    </div>

                    {/* Description */}
                    {item.moduleDescription && (
                      <p className="text-xs text-gray-600 bg-gray-50 p-2.5 rounded-lg border border-gray-100">
                        {item.moduleDescription}
                      </p>
                    )}

                    {/* Tech Stacks */}
                    <div className="text-xs flex items-center gap-2 flex-wrap text-gray-600">
                      {item.moduleTechnology && (
                        <div className="flex items-center gap-1 font-mono text-[11px] bg-slate-100 px-2 py-0.5 rounded text-slate-800">
                          <span>⚙️ Tech:</span>
                          <strong>{item.moduleTechnology}</strong>
                        </div>
                      )}
                      {item.projectTechnologyStack && (
                        <div className="text-[11px] text-gray-500">
                          Stack: {item.projectTechnologyStack}
                        </div>
                      )}
                    </div>

                    {/* Project Manager & Assigned At */}
                    <div className="pt-2 border-t border-gray-100 text-xs text-gray-500 flex flex-wrap justify-between items-center gap-2">
                      <div>
                        👤 PM: <strong className="text-gray-700">{item.projectManagerName || 'Project Manager'}</strong>
                        {item.projectManagerEmail && (
                          <span className="text-gray-400 text-[11px] ml-1">({item.projectManagerEmail})</span>
                        )}
                      </div>
                      {item.assignedAt && (
                        <div className="text-[11px] text-gray-400">
                          Assigned: {new Date(item.assignedAt).toLocaleDateString()}
                        </div>
                      )}
                    </div>

                    {/* Module Team Members */}
                    {item.teamMemberNames && item.teamMemberNames.length > 0 && (
                      <div className="text-xs pt-1">
                        <span className="text-gray-400 text-[11px] font-medium block mb-1">Module Team Members:</span>
                        <div className="flex flex-wrap gap-1.5">
                          {item.teamMemberNames.map((name, idx) => {
                            const isMe = name === user.fullName || name === profile?.name;
                            return (
                              <span
                                key={idx}
                                className={`px-2 py-0.5 rounded text-[11px] font-medium ${
                                  isMe
                                    ? 'bg-blue-100 text-blue-900 border border-blue-300 font-bold'
                                    : 'bg-gray-100 text-gray-700'
                                }`}
                              >
                                {name} {isMe && '(You)'}
                              </span>
                            );
                          })}
                        </div>
                      </div>
                    )}
                  </div>

                  {/* Card Footer: Bug Stats & Action Buttons */}
                  <div className="mt-4 pt-3 border-t border-gray-100 flex items-center justify-between gap-3">
                    <div className="flex items-center gap-2 text-xs">
                      <span className="bg-indigo-50 text-indigo-700 px-2 py-0.5 rounded font-semibold border border-indigo-100">
                        🐛 {item.myAssignedBugs} bugs assigned to you
                      </span>
                      <span className="text-gray-400 text-[11px]">
                        ({item.activeModuleBugs} active in module)
                      </span>
                    </div>

                    <div className="flex items-center gap-2">
                      <button
                        onClick={() => {
                          setSelectedModuleFilter(selectedModuleFilter === String(item.moduleId) ? '' : String(item.moduleId));
                        }}
                        className={`text-xs px-2.5 py-1 rounded font-semibold transition ${
                          selectedModuleFilter === String(item.moduleId)
                            ? 'bg-purple-600 text-white'
                            : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                        }`}
                      >
                        {selectedModuleFilter === String(item.moduleId) ? '✓ Filtering' : 'Filter Bugs'}
                      </button>

                      <Link
                        to={`/projects/${item.projectId}`}
                        className="text-xs bg-blue-600 hover:bg-blue-700 text-white px-3 py-1 rounded font-semibold transition flex items-center gap-1 shadow-sm"
                      >
                        <span>View Project</span>
                        <span>→</span>
                      </Link>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </section>

        {/* SECTION 2: ASSIGNED BUGS & SLA WORKFLOW */}
        <section id="assigned-bugs" className="space-y-4">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 border-b pb-3">
            <div>
              <div className="flex items-center gap-2">
                <span className="text-xl">🎯</span>
                <h2 className="text-lg font-bold text-gray-900">My Assigned Bug Fixes & SLA Queue</h2>
                <span className="bg-purple-100 text-purple-800 text-xs px-2.5 py-0.5 rounded-full font-bold">
                  {bugs.length} {bugs.length === 1 ? 'Bug' : 'Bugs'}
                </span>
                {selectedModuleFilter && (
                  <span className="bg-blue-100 text-blue-800 text-xs px-2.5 py-0.5 rounded-full font-semibold">
                    Filtered by Module #{selectedModuleFilter}
                  </span>
                )}
              </div>
              <p className="text-xs text-gray-500 mt-0.5">
                Bugs assigned directly to you for investigation, development, and resolution with SLA tracking.
              </p>
            </div>
          </div>

          {/* Filter Toolbar */}
          <div className="bg-white p-4 rounded-xl border border-gray-200 shadow-sm flex flex-wrap gap-4 items-center justify-between">
            <div className="flex flex-wrap gap-3 items-center flex-1">
              {/* Status filter */}
              <div>
                <label className="block text-xs font-medium text-gray-500 mb-1">Status</label>
                <select
                  value={selectedStatus}
                  onChange={(e) => setSelectedStatus(e.target.value)}
                  className="px-3 py-1.5 border border-gray-300 rounded-lg text-xs bg-white focus:ring-2 focus:ring-blue-500"
                >
                  <option value="">All Statuses</option>
                  <option value="ASSIGNED">Assigned</option>
                  <option value="IN_PROGRESS">In Progress</option>
                  <option value="RESOLVED">Resolved</option>
                  <option value="CLOSED">Closed</option>
                  <option value="REOPENED">Reopened</option>
                </select>
              </div>

              {/* Severity filter */}
              <div>
                <label className="block text-xs font-medium text-gray-500 mb-1">Severity</label>
                <select
                  value={selectedSeverity}
                  onChange={(e) => setSelectedSeverity(e.target.value)}
                  className="px-3 py-1.5 border border-gray-300 rounded-lg text-xs bg-white focus:ring-2 focus:ring-blue-500"
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
                  className="px-3 py-1.5 border border-gray-300 rounded-lg text-xs bg-white focus:ring-2 focus:ring-blue-500"
                >
                  <option value="">All SLA Statuses</option>
                  <option value="ON_TRACK">On Track</option>
                  <option value="AT_RISK">At Risk</option>
                  <option value="BREACHED">Breached</option>
                </select>
              </div>

              {(selectedStatus || selectedSeverity || selectedSlaStatus || selectedModuleFilter) && (
                <button
                  onClick={() => {
                    setSelectedStatus('');
                    setSelectedSeverity('');
                    setSelectedSlaStatus('');
                    setSelectedModuleFilter('');
                  }}
                  className="mt-5 text-xs text-red-600 hover:text-red-800 font-semibold underline"
                >
                  Clear Filters
                </button>
              )}
            </div>
          </div>

          {/* Bugs Table */}
          <div className="bg-white rounded-xl border border-gray-200 shadow-sm overflow-hidden">
            {loading ? (
              <div className="text-center py-16 text-gray-500 text-sm">Loading your assigned bugs...</div>
            ) : bugs.length === 0 ? (
              <div className="text-center py-16 px-4">
                <div className="text-4xl mb-3">🎉</div>
                <h3 className="text-base font-bold text-gray-800 mb-1">No assigned bugs found</h3>
                <p className="text-xs text-gray-500 max-w-md mx-auto">
                  {assignedWork.length > 0
                    ? `You are assigned to ${assignedWork.length} module(s) above. Currently no open bugs are assigned directly to you.`
                    : 'You currently have no open bugs assigned to you.'}
                </p>
              </div>
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full text-left">
                  <thead className="bg-gray-50 border-b text-xs font-semibold text-gray-500 uppercase tracking-wider">
                    <tr>
                      <th className="py-3.5 px-4">Bug ID</th>
                      <th className="py-3.5 px-4">Title</th>
                      <th className="py-3.5 px-4">Project / Module</th>
                      <th className="py-3.5 px-4">Severity</th>
                      <th className="py-3.5 px-4">Status</th>
                      <th className="py-3.5 px-4">SLA State</th>
                      <th className="py-3.5 px-4">SLA Remaining</th>
                      <th className="py-3.5 px-4 text-right">Workflow Actions</th>
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
                            {bug.resolutionNotes && (
                              <div className="text-xs text-purple-700 bg-purple-50 px-2 py-0.5 rounded mt-1 inline-block border border-purple-200">
                                Notes: {bug.resolutionNotes}
                              </div>
                            )}
                          </td>
                          <td className="py-3.5 px-4 text-gray-700 text-xs">
                            <div className="font-semibold text-gray-900">{bug.projectName}</div>
                            <div className="text-gray-500">{bug.moduleName}</div>
                          </td>
                          <td className="py-3.5 px-4">{getSeverityBadge(bug.severity)}</td>
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
                          <td className="py-3.5 px-4 text-right">
                            <div className="flex justify-end items-center gap-2">
                              {(bug.status === 'ASSIGNED' || bug.status === 'REOPENED') && (
                                <button
                                  onClick={() => handleStartWork(bug.id)}
                                  className="bg-blue-600 hover:bg-blue-700 text-white px-3 py-1.5 rounded-lg text-xs font-bold shadow-sm transition-colors"
                                >
                                  Start Work
                                </button>
                              )}
                              {bug.status === 'IN_PROGRESS' && (
                                <button
                                  onClick={() => handleOpenResolveModal(bug)}
                                  className="bg-purple-600 hover:bg-purple-700 text-white px-3 py-1.5 rounded-lg text-xs font-bold shadow-sm transition-colors"
                                >
                                  Submit Fix
                                </button>
                              )}
                              <Link
                                to={`/bugs/${bug.id}`}
                                className="bg-gray-100 hover:bg-gray-200 text-gray-700 px-2.5 py-1.5 rounded-lg text-xs font-medium"
                              >
                                Details
                              </Link>
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
        </section>
      </main>

      {/* Submit Fix / Resolution Notes Modal */}
      {resolveModalBug && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-xl max-w-lg w-full p-6 shadow-xl border border-gray-200 animate-in fade-in zoom-in duration-150">
            <div className="flex justify-between items-center mb-4">
              <h3 className="text-lg font-bold text-gray-900">
                Submit Fix for Bug #{resolveModalBug.id}
              </h3>
              <button
                onClick={handleCloseResolveModal}
                className="text-gray-400 hover:text-gray-600 text-lg font-bold"
              >
                ✕
              </button>
            </div>

            <div className="mb-4 bg-gray-50 p-3 rounded-lg border text-xs text-gray-700">
              <span className="font-semibold block mb-1">Title: {resolveModalBug.title}</span>
              <span className="text-gray-500">Module: {resolveModalBug.moduleName}</span>
            </div>

            <form onSubmit={handleSubmitResolution} className="space-y-4">
              <div>
                <label className="block text-sm font-semibold text-gray-700 mb-1">
                  Resolution Notes <span className="text-red-500">*</span>
                </label>
                <textarea
                  rows="4"
                  value={resolutionNotes}
                  onChange={(e) => setResolutionNotes(e.target.value)}
                  placeholder="Explain what caused the bug, what fix was applied, and any testing guidance for QA..."
                  className="w-full px-3.5 py-2.5 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-purple-500"
                  required
                />
                <p className="text-xs text-gray-500 mt-1">
                  These notes will be reviewed by the QA Tester to verify the fix and close the bug.
                </p>
              </div>

              <div className="flex justify-end gap-2 pt-4 border-t">
                <button
                  type="button"
                  onClick={handleCloseResolveModal}
                  className="px-4 py-2 border border-gray-300 rounded-lg text-sm font-semibold text-gray-700 hover:bg-gray-50"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={submitting}
                  className="px-5 py-2 bg-purple-600 text-white rounded-lg text-sm font-semibold hover:bg-purple-700 shadow-sm disabled:opacity-50"
                >
                  {submitting ? 'Submitting...' : 'Mark as Resolved'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

export default DeveloperBugs;
