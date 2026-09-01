import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { developersAPI } from '../api/api';

function DeveloperLeaderboard() {
  const [leaderboard, setLeaderboard] = useState([]);
  const [loading, setLoading] = useState(true);
  const [sortBy, setSortBy] = useState('bugsResolved');
  const [sortOrder, setSortOrder] = useState('desc');
  const [selectedDeveloperHistory, setSelectedDeveloperHistory] = useState(null);
  const [historyList, setHistoryList] = useState([]);
  const [historyLoading, setHistoryLoading] = useState(false);

  const user = JSON.parse(localStorage.getItem('user') || '{}');

  useEffect(() => {
    fetchLeaderboard();
  }, [sortBy, sortOrder]);

  const fetchLeaderboard = async () => {
    setLoading(true);
    try {
      const res = await developersAPI.getLeaderboard({ sortBy, sortOrder });
      setLeaderboard(res.data.data || []);
    } catch (err) {
      console.error('Error fetching leaderboard:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleSortChange = (field) => {
    if (sortBy === field) {
      setSortOrder(sortOrder === 'desc' ? 'asc' : 'desc');
    } else {
      setSortBy(field);
      setSortOrder('desc');
    }
  };

  const handleViewHistory = async (dev) => {
    setSelectedDeveloperHistory(dev);
    setHistoryLoading(true);
    try {
      const res = await developersAPI.getAssignmentHistory(dev.developerId);
      setHistoryList(res.data.data || []);
    } catch (err) {
      console.error('Error fetching developer assignment history:', err);
    } finally {
      setHistoryLoading(false);
    }
  };

  const getSortIcon = (field) => {
    if (sortBy !== field) return '↕';
    return sortOrder === 'desc' ? '↓' : '↑';
  };

  return (
    <div className="min-h-screen bg-gray-50 pb-16">
      {/* Header */}
      <header className="bg-white shadow-sm border-b">
        <div className="max-w-7xl mx-auto px-4 py-4 flex justify-between items-center">
          <div className="flex items-center gap-4">
            <Link to="/" className="text-blue-600 hover:text-blue-800 text-sm font-semibold">
              ← Dashboard
            </Link>
            <span className="text-gray-300">|</span>
            <h1 className="text-xl font-bold text-gray-900 flex items-center gap-2">
              <span>📊</span> Developer Performance & Metrics Leaderboard
            </h1>
          </div>
          <div className="flex items-center gap-3">
            <Link
              to="/developers"
              className="bg-gray-100 hover:bg-gray-200 text-gray-700 px-3 py-1.5 rounded-lg text-xs font-semibold"
            >
              Developer Management
            </Link>
            <Link
              to="/sla-rules"
              className="bg-gray-100 hover:bg-gray-200 text-gray-700 px-3 py-1.5 rounded-lg text-xs font-semibold"
            >
              SLA Policies
            </Link>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 py-6">
        {/* Informational Banner */}
        <div className="bg-indigo-50 border border-indigo-200 text-indigo-950 p-4 rounded-xl mb-6 shadow-sm flex items-start gap-3">
          <span className="text-xl">ℹ️</span>
          <div className="text-xs space-y-1">
            <p className="font-bold text-sm text-indigo-900">Historical Developer Metrics & Assessment</p>
            <p className="text-indigo-800">
              This leaderboard presents purely historical performance data and live workload tracking for all developers.
              Developers without resolution history are clearly marked with <em>"No data yet"</em> to ensure statistical accuracy.
            </p>
          </div>
        </div>

        {/* Sort / Filter Bar */}
        <div className="bg-white p-4 rounded-xl border border-gray-200 shadow-sm mb-6 flex flex-wrap gap-4 items-center justify-between">
          <div className="flex items-center gap-2 text-xs font-medium text-gray-600">
            <span>Sort by:</span>
            <button
              onClick={() => handleSortChange('bugsResolved')}
              className={`px-3 py-1.5 rounded-lg border text-xs font-semibold transition-colors ${
                sortBy === 'bugsResolved'
                  ? 'bg-blue-600 text-white border-blue-600'
                  : 'bg-white text-gray-700 hover:bg-gray-50 border-gray-300'
              }`}
            >
              Bugs Resolved {sortBy === 'bugsResolved' && (sortOrder === 'desc' ? '↓' : '↑')}
            </button>
            <button
              onClick={() => handleSortChange('firstTimeFixRate')}
              className={`px-3 py-1.5 rounded-lg border text-xs font-semibold transition-colors ${
                sortBy === 'firstTimeFixRate'
                  ? 'bg-blue-600 text-white border-blue-600'
                  : 'bg-white text-gray-700 hover:bg-gray-50 border-gray-300'
              }`}
            >
              First-Time Fix Rate {sortBy === 'firstTimeFixRate' && (sortOrder === 'desc' ? '↓' : '↑')}
            </button>
            <button
              onClick={() => handleSortChange('averageResolutionTimeHours')}
              className={`px-3 py-1.5 rounded-lg border text-xs font-semibold transition-colors ${
                sortBy === 'averageResolutionTimeHours'
                  ? 'bg-blue-600 text-white border-blue-600'
                  : 'bg-white text-gray-700 hover:bg-gray-50 border-gray-300'
              }`}
            >
              Avg Resolution Time {sortBy === 'averageResolutionTimeHours' && (sortOrder === 'desc' ? '↓' : '↑')}
            </button>
            <button
              onClick={() => handleSortChange('slaComplianceRate')}
              className={`px-3 py-1.5 rounded-lg border text-xs font-semibold transition-colors ${
                sortBy === 'slaComplianceRate'
                  ? 'bg-blue-600 text-white border-blue-600'
                  : 'bg-white text-gray-700 hover:bg-gray-50 border-gray-300'
              }`}
            >
              SLA Compliance {sortBy === 'slaComplianceRate' && (sortOrder === 'desc' ? '↓' : '↑')}
            </button>
            <button
              onClick={() => handleSortChange('currentWorkload')}
              className={`px-3 py-1.5 rounded-lg border text-xs font-semibold transition-colors ${
                sortBy === 'currentWorkload'
                  ? 'bg-blue-600 text-white border-blue-600'
                  : 'bg-white text-gray-700 hover:bg-gray-50 border-gray-300'
              }`}
            >
              Workload {sortBy === 'currentWorkload' && (sortOrder === 'desc' ? '↓' : '↑')}
            </button>
          </div>

          <div className="text-xs text-gray-500">
            Total Developers: <strong>{leaderboard.length}</strong>
          </div>
        </div>

        {/* Leaderboard Table */}
        <div className="bg-white rounded-xl border border-gray-200 shadow-sm overflow-hidden">
          {loading ? (
            <div className="text-center py-16 text-gray-500">Loading leaderboard metrics...</div>
          ) : leaderboard.length === 0 ? (
            <div className="text-center py-16 text-gray-500">No developers found.</div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left">
                <thead className="bg-gray-50 border-b text-xs font-semibold text-gray-500 uppercase tracking-wider">
                  <tr>
                    <th className="py-3.5 px-4"># Rank</th>
                    <th className="py-3.5 px-4 cursor-pointer" onClick={() => handleSortChange('developerName')}>
                      Developer {getSortIcon('developerName')}
                    </th>
                    <th className="py-3.5 px-4 cursor-pointer" onClick={() => handleSortChange('bugsResolved')}>
                      Bugs Resolved {getSortIcon('bugsResolved')}
                    </th>
                    <th className="py-3.5 px-4 cursor-pointer" onClick={() => handleSortChange('firstTimeFixRate')}>
                      First-Time Fix Rate {getSortIcon('firstTimeFixRate')}
                    </th>
                    <th className="py-3.5 px-4 cursor-pointer" onClick={() => handleSortChange('averageResolutionTimeHours')}>
                      Avg Res. Time {getSortIcon('averageResolutionTimeHours')}
                    </th>
                    <th className="py-3.5 px-4 cursor-pointer" onClick={() => handleSortChange('slaComplianceRate')}>
                      SLA Compliance {getSortIcon('slaComplianceRate')}
                    </th>
                    <th className="py-3.5 px-4 cursor-pointer" onClick={() => handleSortChange('currentWorkload')}>
                      Live Workload {getSortIcon('currentWorkload')}
                    </th>
                    <th className="py-3.5 px-4 cursor-pointer" onClick={() => handleSortChange('totalBugsHandled')}>
                      Total Handled {getSortIcon('totalBugsHandled')}
                    </th>
                    <th className="py-3.5 px-4 text-right">History</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100 text-sm">
                  {leaderboard.map((item, idx) => {
                    const hasHistory = item.hasHistory;
                    const fixRate = item.firstTimeFixRate;
                    const avgTime = item.averageResolutionTimeHours;
                    const slaRate = item.slaComplianceRate;

                    return (
                      <tr key={item.developerId} className="hover:bg-gray-50/80 transition-colors">
                        <td className="py-3.5 px-4 font-mono text-xs text-gray-400">
                          {idx === 0 && item.bugsResolved > 0 ? '🥇 1' : idx === 1 && item.bugsResolved > 0 ? '🥈 2' : idx === 2 && item.bugsResolved > 0 ? '🥉 3' : idx + 1}
                        </td>
                        <td className="py-3.5 px-4">
                          <div className="font-semibold text-gray-900">{item.developerName}</div>
                          <div className="text-xs text-gray-400">{item.developerEmail}</div>
                          {item.skillsTechStack && (
                            <div className="text-[11px] text-gray-500 mt-0.5">{item.skillsTechStack}</div>
                          )}
                        </td>
                        <td className="py-3.5 px-4">
                          <span className="font-bold text-base text-gray-900">{item.bugsResolved}</span>
                          {item.bugsReopened > 0 && (
                            <span className="text-xs text-red-600 block">({item.bugsReopened} reopened)</span>
                          )}
                        </td>
                        <td className="py-3.5 px-4">
                          {fixRate !== null ? (
                            <div className="flex items-center gap-1.5">
                              <span className={`font-bold ${fixRate >= 80 ? 'text-green-700' : fixRate >= 50 ? 'text-amber-700' : 'text-red-600'}`}>
                                {fixRate}%
                              </span>
                            </div>
                          ) : (
                            <span className="text-gray-400 italic text-xs">No data yet</span>
                          )}
                        </td>
                        <td className="py-3.5 px-4 font-mono text-xs">
                          {avgTime !== null ? (
                            <span>{avgTime} hrs</span>
                          ) : (
                            <span className="text-gray-400 italic font-sans text-xs">No data yet</span>
                          )}
                        </td>
                        <td className="py-3.5 px-4">
                          {slaRate !== null ? (
                            <span className={`font-semibold ${slaRate >= 85 ? 'text-emerald-700' : 'text-amber-700'}`}>
                              {slaRate}%
                            </span>
                          ) : (
                            <span className="text-gray-400 italic text-xs">No data yet</span>
                          )}
                        </td>
                        <td className="py-3.5 px-4">
                          <span className={`px-2.5 py-1 rounded-full text-xs font-bold ${
                            item.currentWorkload > 0
                              ? 'bg-blue-100 text-blue-800 border border-blue-200'
                              : 'bg-gray-100 text-gray-600'
                          }`}>
                            {item.currentWorkload} active
                          </span>
                        </td>
                        <td className="py-3.5 px-4 text-xs text-gray-600">
                          {item.totalBugsHandled}
                        </td>
                        <td className="py-3.5 px-4 text-right">
                          <button
                            onClick={() => handleViewHistory(item)}
                            className="bg-gray-100 hover:bg-gray-200 text-gray-700 px-2.5 py-1.5 rounded-lg text-xs font-medium"
                          >
                            Log
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

      {/* Assignment History Modal */}
      {selectedDeveloperHistory && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-xl max-w-2xl w-full p-6 shadow-xl border border-gray-200 animate-in fade-in zoom-in duration-150 max-h-[85vh] flex flex-col">
            <div className="flex justify-between items-center mb-4 pb-3 border-b">
              <div>
                <h3 className="text-lg font-bold text-gray-900">
                  Assignment History — {selectedDeveloperHistory.developerName}
                </h3>
                <p className="text-xs text-gray-500">{selectedDeveloperHistory.developerEmail}</p>
              </div>
              <button
                onClick={() => setSelectedDeveloperHistory(null)}
                className="text-gray-400 hover:text-gray-600 text-lg font-bold"
              >
                ✕
              </button>
            </div>

            <div className="overflow-y-auto flex-1">
              {historyLoading ? (
                <div className="text-center py-10 text-gray-500">Loading history...</div>
              ) : historyList.length === 0 ? (
                <div className="text-center py-10 text-gray-400">No assignment history logged for this developer.</div>
              ) : (
                <table className="w-full text-left text-xs">
                  <thead className="bg-gray-50 border-b text-gray-500 uppercase font-semibold">
                    <tr>
                      <th className="py-2.5 px-3">Bug</th>
                      <th className="py-2.5 px-3">Assigned At</th>
                      <th className="py-2.5 px-3">Unassigned At</th>
                      <th className="py-2.5 px-3">Assigned By</th>
                      <th className="py-2.5 px-3">Status</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-100">
                    {historyList.map((h) => (
                      <tr key={h.id} className="hover:bg-gray-50">
                        <td className="py-2.5 px-3">
                          <Link to={`/bugs/${h.bugId}`} className="text-blue-600 hover:underline font-semibold">
                            #{h.bugId} {h.bugTitle}
                          </Link>
                        </td>
                        <td className="py-2.5 px-3 text-gray-600">
                          {h.assignedAt ? new Date(h.assignedAt).toLocaleString() : '-'}
                        </td>
                        <td className="py-2.5 px-3 text-gray-600">
                          {h.unassignedAt ? new Date(h.unassignedAt).toLocaleString() : '-'}
                        </td>
                        <td className="py-2.5 px-3 text-gray-700">{h.assignedByName}</td>
                        <td className="py-2.5 px-3">
                          {h.isCurrent ? (
                            <span className="bg-green-100 text-green-800 px-2 py-0.5 rounded font-bold text-[11px]">
                              Active
                            </span>
                          ) : (
                            <span className="bg-gray-100 text-gray-600 px-2 py-0.5 rounded text-[11px]">
                              Released
                            </span>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>

            <div className="pt-4 border-t flex justify-end">
              <button
                onClick={() => setSelectedDeveloperHistory(null)}
                className="px-4 py-2 bg-gray-100 hover:bg-gray-200 text-gray-700 rounded-lg text-xs font-semibold"
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default DeveloperLeaderboard;
