using System;
using System.Linq;
using System.Collections.Generic;
using Windows.Foundation.Collections;
using Fmg.Common;
using Fmg.Common.Geom;
using Fmg.Core.App.Model;
using Fmg.Core.App.Serializers;
using Fmg.Core.Types;
using Fmg.Core.Mosaic.Cells;

namespace Fmg.Uwp.App.Serializers {

    /// <summary> Mosaic cells data (de)serializer. For save/restore <see cref="MosaicBackupData"/> </summary>
    public class MosaicBackupDataSerializer : ISerializer {

        private const int VERSION = 1;

        private const           string KEY__MOSAIC_BACKUP_DATA__SECTION             = nameof(MosaicBackupData);
        private const           string KEY__MOSAIC_BACKUP_DATA__VERSION             = "Version";
        private static readonly string KEY__MOSAIC_BACKUP_DATA__SIZE_FIELD_M        = nameof(MosaicBackupData.SizeField) + '.' + nameof(MosaicBackupData.SizeField.m);
        private static readonly string KEY__MOSAIC_BACKUP_DATA__SIZE_FIELD_N        = nameof(MosaicBackupData.SizeField) + '.' + nameof(MosaicBackupData.SizeField.n);
        private const           string KEY__MOSAIC_BACKUP_DATA__MOSAIC_TYPE         = nameof(MosaicBackupData.MosaicType);
        private static readonly string KEY__MOSAIC_BACKUP_DATA__CELL_STATES__STATUS = nameof(MosaicBackupData.CellStates) + '.' + nameof(BaseCell.StateCell.Status);
        private static readonly string KEY__MOSAIC_BACKUP_DATA__CELL_STATES__OPEN   = nameof(MosaicBackupData.CellStates) + '.' + nameof(BaseCell.StateCell.Open);
        private static readonly string KEY__MOSAIC_BACKUP_DATA__CELL_STATES__CLOSE  = nameof(MosaicBackupData.CellStates) + '.' + nameof(BaseCell.StateCell.Close);
        private static readonly string KEY__MOSAIC_BACKUP_DATA__CELL_STATES__DOWN   = nameof(MosaicBackupData.CellStates) + '.' + nameof(BaseCell.StateCell.Down);
        private const           string KEY__MOSAIC_BACKUP_DATA__AREA                = nameof(MosaicBackupData.Area);
        private const           string KEY__MOSAIC_BACKUP_DATA__CLICK_COUNT         = nameof(MosaicBackupData.ClickCount);

        internal MosaicBackupData Load(IPropertySet from) {
            if ((from != null) && from.ContainsKey(KEY__MOSAIC_BACKUP_DATA__SECTION)) try {
                var section = (Windows.Storage.ApplicationDataCompositeValue)from[KEY__MOSAIC_BACKUP_DATA__SECTION];
                if (!section.ContainsKey(KEY__MOSAIC_BACKUP_DATA__VERSION))
                    return null;

                int version = (int)section[KEY__MOSAIC_BACKUP_DATA__VERSION];
                if (version != 1)
                    throw new ArgumentException("MosaicBackupDataSerializer: Version #" + version + " is not supported");

                int mosaicOrdinal = (int)section[KEY__MOSAIC_BACKUP_DATA__MOSAIC_TYPE];
                int sizeFieldM    = (int)section[KEY__MOSAIC_BACKUP_DATA__SIZE_FIELD_M];
                int sizeFieldN    = (int)section[KEY__MOSAIC_BACKUP_DATA__SIZE_FIELD_N];
                double area    = (double)section[KEY__MOSAIC_BACKUP_DATA__AREA];
                int clickCount    = (int)section[KEY__MOSAIC_BACKUP_DATA__CLICK_COUNT];

                IList<EState> states = ((string)section[KEY__MOSAIC_BACKUP_DATA__CELL_STATES__STATUS]).Split(',').Select(v => Enum.Parse<EState>(v)).ToList();
                IList<EOpen > opens  = ((string)section[KEY__MOSAIC_BACKUP_DATA__CELL_STATES__OPEN  ]).Split(',').Select(v => Enum.Parse<EOpen >(v)).ToList();
                IList<EClose> closes = ((string)section[KEY__MOSAIC_BACKUP_DATA__CELL_STATES__CLOSE ]).Split(',').Select(v => Enum.Parse<EClose>(v)).ToList();
                IList< bool > downs  = ((string)section[KEY__MOSAIC_BACKUP_DATA__CELL_STATES__DOWN  ]).Split(',').Select(v => bool.Parse        (v)).ToList();

                System.Diagnostics.Debug.Assert(states.Count == sizeFieldM * sizeFieldN);
                System.Diagnostics.Debug.Assert(states.Count == opens.Count);
                System.Diagnostics.Debug.Assert(states.Count == closes.Count);

                MosaicBackupData data = new MosaicBackupData {
                    MosaicType = EMosaicEx.FromOrdinal(mosaicOrdinal),
                    SizeField  = new Matrisize(sizeFieldM, sizeFieldN),
                    Area       = area,
                    ClickCount = clickCount,
                    CellStates = new List<BaseCell.StateCell>(states.Count)
                };
                for (int i = 0; i < states.Count; ++i) {
                    data.CellStates.Add(new BaseCell.StateCell {
                        Status = states[i],
                        Open   = opens [i],
                        Close  = closes[i],
                        Down   = downs [i]
                    });
                }
                return data;

            } catch(Exception ex) {
                Logger.Error("Can`t load mosaic backup data from SharedPreferences" + ex);
            }
            return null;
        }

        internal void Save(MosaicBackupData data, IPropertySet to) {
            if (data == null)
                return;

            to[KEY__MOSAIC_BACKUP_DATA__SECTION] = new Windows.Storage.ApplicationDataCompositeValue() {
                [KEY__MOSAIC_BACKUP_DATA__VERSION            ] = VERSION,
                [KEY__MOSAIC_BACKUP_DATA__MOSAIC_TYPE        ] = data.MosaicType.Ordinal(),
                [KEY__MOSAIC_BACKUP_DATA__SIZE_FIELD_M       ] = data.SizeField.m,
                [KEY__MOSAIC_BACKUP_DATA__SIZE_FIELD_N       ] = data.SizeField.n,
                [KEY__MOSAIC_BACKUP_DATA__CELL_STATES__STATUS] = string.Join(",", data.CellStates.Select(c => c.Status.ToString())),
                [KEY__MOSAIC_BACKUP_DATA__CELL_STATES__OPEN  ] = string.Join(",", data.CellStates.Select(c => c.Open  .ToString())),
                [KEY__MOSAIC_BACKUP_DATA__CELL_STATES__CLOSE ] = string.Join(",", data.CellStates.Select(c => c.Close .ToString())),
                [KEY__MOSAIC_BACKUP_DATA__CELL_STATES__DOWN  ] = string.Join(",", data.CellStates.Select(c => c.Down  .ToString())),
                [KEY__MOSAIC_BACKUP_DATA__AREA               ] = data.Area,
                [KEY__MOSAIC_BACKUP_DATA__CLICK_COUNT        ] = data.ClickCount
            };
        }

    }

}
