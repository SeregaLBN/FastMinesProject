using System.Collections.Generic;
using Fmg.Common.Geom;
using Fmg.Core.Types;
using Fmg.Core.Mosaic.Cells;

namespace Fmg.Core.App.Model {

    /// <summary> Mosaic controller backup data </summary>
    public class MosaicBackupData {

        public EMosaic MosaicType { get; set; }

        public Matrisize SizeField { get; set; }

        public IList<BaseCell.StateCell> CellStates { get; set; }

        public double Area { get; set; }

        public int ClickCount { get; set; }

    }

}