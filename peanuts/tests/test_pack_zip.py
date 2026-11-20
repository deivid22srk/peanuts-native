"""A game's compiled pack travels: Export pack… / Import pack… (PackZip, on the JVM with android/app/src/jvmtest's PackZipJvmTest; skipped without a JDK), and the
wiring: Add game imports a pack .zip beside the game when no pack folder plays with it (the hook between Folders.find and DumpLayout.find, packZipToPack's checks),
and the Manage sheet keeps its Export pack… / Import pack… rows."""
import shutil
import subprocess
import tempfile
import unittest
from pathlib import Path

REPO = Path(__file__).resolve().parents[2]
SRC = REPO / 'android/app/src/main/java/dev/nxnative/android'
TEST = REPO / 'android/app/src/jvmtest/java/dev/nxnative/android/PackZipJvmTest.java'
MAIN = REPO / 'android/app/src/main/java/dev/nxnative/android/MainActivity.java'
STUB = '''package dev.nxnative.android;
public final class BuildConfig {
    public static final String NX_PROFILES = "040074A01BF12000\\tDonkey Kong Bananza\\t22852\\tabcdef012345\\t458752 main=aa\\n0400C3F00006E000\\tMario Kart World\\t0\\tunknown\\t";
    public static final String NX_DMSH_VK_CHAIN = "c0ffee", NX_DMSH_VK_ABI = "abi1", NX_DMSH_VK_ABI_COMPAT = "";
    public static final boolean NX_PLAY = false;
}
'''


@unittest.skipUnless(shutil.which('javac') and shutil.which('java'), 'no JDK')
class Jvm(unittest.TestCase):
    def test_pack_zip(self):
        with tempfile.TemporaryDirectory(prefix='packzip_') as d:
            d = Path(d)
            (d / 'stub').mkdir()
            (d / 'stub/BuildConfig.java').write_text(STUB)
            srcs = [SRC / f'{n}.java' for n in ('PackZip', 'Pack', 'PackFind', 'DumpLayout', 'BuildCheck', 'RomfsCheck', 'Nup', 'Nsp', 'Zstd', 'Profile', 'ShaderLibs', 'SaveStore')]
            r = subprocess.run(['javac', '-encoding', 'UTF-8', '-Xlint:all,-try', '-Werror', '-d', str(d / 'classes'), *map(str, srcs), str(TEST), str(d / 'stub/BuildConfig.java')],
                               capture_output=True, text=True)
            self.assertEqual(r.returncode, 0, r.stdout + r.stderr)
            (d / 'run').mkdir()
            r = subprocess.run(['java', '-cp', str(d / 'classes'), 'dev.nxnative.android.PackZipJvmTest', str(d / 'run')], capture_output=True, text=True)
            self.assertEqual(r.returncode, 0, r.stdout + r.stderr)
            self.assertRegex(r.stdout, r'^ok \d+')


class Wiring(unittest.TestCase):
    def test_add_game_imports_a_pack_zip_beside_the_game(self):
        main = MAIN.read_text(encoding='utf-8')
        # the hook sits between the pack folder search and the dump check, and re-runs the search when the zip imported
        self.assertIn('if (k.pack == null && packZipToPack(g, k.id) != null)', main)
        self.assertIn('k.pack = Folders.find(this, pd, g, k.id);   // the imported pack, as the app\'s packs/<id> candidate this time', main)
        helper = main[main.index('private Pack packZipToPack('):]
        # the checks of Import pack… run here too, and a failure leaves nothing behind and goes on without a pack
        self.assertIn('PackZip.exportZip(game, id)', helper)
        self.assertIn('PackUi.busy(id)', helper)
        self.assertIn('Job.remembered(this)', helper)
        self.assertIn('getUsableSpace() < z.length() + (256L << 20)', helper)
        self.assertIn('PackZip.stage(z, packs, id)', helper)
        self.assertIn('PackCheck.blocking(this, p, game)', helper)
        self.assertIn('PackZip.swapIn(p.dir, target)', helper)
        self.assertRegex(helper, r'catch \(IOException \| RuntimeException e\)')

    def test_manage_sheet_keeps_export_and_import(self):
        main = MAIN.read_text(encoding='utf-8')
        self.assertIn('"Export pack…"', main)
        self.assertIn('PackUi.exportPick(this, g)', main)
        self.assertIn('"Import pack…"', main)
        self.assertIn('PackUi.importPick(this, g)', main)

    def test_export_zip_walks_where_pack_folders_are_searched(self):
        packzip = (SRC / 'PackZip.java').read_text(encoding='utf-8')
        self.assertIn('static File exportZip(File game, String id)', packzip)
        self.assertIn('PackFind.volumeRoot', packzip)
        self.assertIn('PackFind.LEVELS', packzip)
        self.assertIn('banana-pack-', packzip)
        self.assertRegex(packzip, r'\.startsWith\("\."')


if __name__ == '__main__':
    unittest.main()
