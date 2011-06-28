using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Shuttle_Tracker_Service
{
    class Eta
    {
        private Dictionary<Key, List<int>> m_etas;

        private struct Key
        {
            public readonly int routeId;
            public readonly string stopId;
            public Key(int routeId, string stopId)
            {
                this.routeId = routeId;
                this.stopId = stopId;
            }
        }

        public void clearEta(Route r, Stop s)
        {
            m_etas.Remove(new Key(r.Id, s.Id));
        }

        public void clearAllEta()
        {
            m_etas.Clear();
        }

        public void addEta(Route r, Stop s, int eta)
        {
            List<int> l;

            if (!m_etas.TryGetValue(new Key(r.Id, s.Id), out l))
            {
                l = new List<int>();
                m_etas.Add(new Key(r.Id, s.Id), l);
            }

            l.Add(eta);   
        }

        public List<int> getEta(Route r, Stop s)
        {
            return m_etas[new Key(r.Id, s.Id)];
        }
    }
}
