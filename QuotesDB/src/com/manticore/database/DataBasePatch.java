/*
 *
 *  Copyright (C) 2010 Andreas Reichel <andreas@manticore-projects.com>
 *
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or (at
 *  your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 *
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */

package com.manticore.database;

import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataBasePatch implements Comparable<DataBasePatch> {
    public int major_version;
    public int minor_version;
    public int patch_level;

    DataBasePatch(int major_version, int minor_version, int patch_level) {
        this.major_version = major_version;
        this.minor_version = minor_version;
        this.patch_level = patch_level;
    }

    public boolean process() {
        boolean processed=false;
        if (canApplyPatch()) {
            applyPatch();
            processed=true;
        }
        return processed;
    }

    private boolean canApplyPatch() {
        boolean applyPatch = false;
        String sqlStr = "SELECT * FROM trader.version_info ORDER BY major_version DESC, minor_version DESC, patch_level DESC;";
        try {
            ResultSet resultSet = Quotes.getInstance().getConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY).executeQuery(sqlStr);
            if (resultSet.next()) {
                DataBasePatch pl = new DataBasePatch(resultSet.getInt("major_version"), resultSet.getInt("minor_version"), resultSet.getInt("patch_level"));
                applyPatch = compareTo(pl) > 0;
            }
            resultSet.close();
        } catch (SQLException ex) {
            Logger.getLogger(DataBaseWizard.class.getName()).log(Level.SEVERE, null, ex);
        }
        return applyPatch;
    }

    public void applyPatch() {
        String inputStreamUrlStr = DataBaseWizard.buildPatchFileUrlStr(major_version, minor_version, patch_level);
        Logger.getAnonymousLogger().info("apply " + inputStreamUrlStr );
        InputStream inputStream=this.getClass().getResourceAsStream(inputStreamUrlStr);
        String updateScript = DataBaseWizard.getTextFromCompressedInputStream(inputStream);
        Quotes.getInstance().executeSqlBatch(updateScript);
    }

    @Override
    public int compareTo(DataBasePatch o) {
        int compareTo = 0;
        if (major_version < o.major_version) {
            compareTo = -1;
        } else if (major_version > o.major_version) {
            compareTo = 1;
        } else if (major_version == o.major_version && minor_version < o.minor_version) {
            compareTo = -1;
        } else if (major_version == o.major_version && minor_version > o.minor_version) {
            compareTo = 1;
        } else if (major_version == o.major_version && minor_version == o.minor_version && patch_level < o.patch_level) {
            compareTo = -1;
        } else if (major_version == o.major_version && minor_version == o.minor_version && patch_level > o.patch_level) {
            compareTo = 1;
        }
        return compareTo;
    }
}
